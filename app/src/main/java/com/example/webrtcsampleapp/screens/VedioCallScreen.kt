package com.example.webrtcsampleapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(roomID: String, onNavigateBack: () -> Unit) {
    //plan
    //First we will craete a room.
    //Now room is present
    //-- we will check the capacity of the room
    //-- more then 2 -> left back to home.
    //code for setting up signaling server and managing video call
    val executor =
        remember { Executors.newSingleThreadExecutor() }//we use this every where we use Webrtc because we want all the process to execute one by one
    val firestore = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    val eglBase = remember { EglBase.create() }
    var peerConnectionFactory: PeerConnectionFactory? = remember { null }
    var peerConnection: PeerConnection? = remember { null }
    val localCandidatesToShare = remember { arrayListOf<Map<String, Any?>>() }
    val queuedRemoteCandidates = remember { arrayListOf<IceCandidate>() }
    var isOfferer = remember { false }
    var remoteDescriptionSet = remember { false }
    val sdpMediaConstrains: MediaConstraints = remember {
        MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
    }
    var localSdp: SessionDescription? = remember { null }


    // checking room status
    fun checkRoomCapacity(onProceed: () -> Unit) {
        //getting rooms from room id
        val roomDocRef = firestore.collection("rooms").document(roomID)
        roomDocRef.get().addOnSuccessListener { document ->
            Timber.d("Firebase firestore success")
            //check if room exists
            if (document != null && document.exists()) {
                val participantCount = document.getLong("participantCount")?.toInt() ?: 0
                //if room is full
                if (participantCount >= 2) {
                    Toast.makeText(
                        context,
                        "Room is Full. Cannot join at the moment!",
                        Toast.LENGTH_SHORT
                    ).show()

                    //back to home page
                    onNavigateBack.invoke()

                } else {
                    //add another participant
                    roomDocRef.update("participantCount", participantCount + 1)
                    onProceed.invoke()

                }
            } else {
                //create room.
                roomDocRef.set(mapOf("participantCount" to 1))
                //set offerer to true
                isOfferer = true
                onProceed.invoke()
            }
        }.addOnFailureListener {
            // Handle the error
            Timber.e("Firebase Failed to get Firestore DB.")
            Toast.makeText(
                context,
                "Failed to connect to Firebase. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
            onNavigateBack.invoke()
        }
    }

    //WebRTC setup
    fun initializeWebRTC() {
        Timber.d("initializeWebRTC() :: ")

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)//helps for debugging
                .createInitializationOptions()
        )

        //video encoder
        val videoEncoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext, true, false
        )

        //video decoder
        val videoDecoderFactory = DefaultVideoDecoderFactory(
            eglBase.eglBaseContext
        )
//created peer connection factory
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .createPeerConnectionFactory()

    }

    //send signalling message to signalling server
    fun sendSignallingMessage(message: Map<String, Any?>) {
        Timber.d("sendSignallingMessage() :: ")
        val signallingRef = firestore.collection("rooms").document(roomID)
        signallingRef.set(message, SetOptions.merge())
    }

    val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(sessionDescription: SessionDescription?) {
            Timber.d("onCreateSuccess() ::")
            if (localSdp != null) {
                Timber.e("Multiple Session Description Created")
                return
            }
            localSdp = sessionDescription
            executor.execute {
                peerConnection?.setLocalDescription(this, sessionDescription)

            }

        }

        override fun onSetSuccess() {
            if (localSdp == null) {
                return
            }
            executor.execute {
                if (isOfferer) {
                    if (peerConnection?.remoteDescription == null) {//answer not received
                        sendSignallingMessage(
                            mapOf(
                                "type" to "offer",
                                "sdpOffer" to localSdp?.description
                            )
                        )
                    } else {
                        remoteDescriptionSet = true
                        addQueuedCandidates()
                    }
                } else {
                    if (peerConnection?.localDescription != null) {
                        sendSignallingMessage(
                            mapOf(
                                "type" to "answer",
                                "sdpAnswer" to localSdp?.description
                            )
                        )
                        remoteDescriptionSet = true
                        addQueuedCandidates()

                    }
                }
            }

        }

        private fun addQueuedCandidates() {
            queuedRemoteCandidates.forEach {
                peerConnection?.addIceCandidate(it)
            }
            queuedRemoteCandidates.clear()
        }

        override fun onCreateFailure(p0: String?) {

        }

        override fun onSetFailure(p0: String?) {

        }

    }

    fun createAnswer() {
        peerConnection?.createAnswer(sdpObserver, sdpMediaConstrains)
    }


    fun handleSignallingMessages(data: MutableMap<String, Any>) {
        //candidates
        if (isOfferer && data["iceAnswer"] != null) {
            executor.execute {
                val cMDataList = data["iceAnswer"] as List<*>
                cMDataList.forEach { map ->

                    val cData = map as HashMap<*, *>
                    val candidate = IceCandidate(
                        /* sdpMid = */ cData["sdpMid"].toString(),
                        /* sdpMLineIndex = */ cData["sdpMLineIndex"].toString().toInt(),
                        /* sdp = */ cData["candidate"].toString()

                    )

                    //check remote description is set or not
                    if (remoteDescriptionSet) {
                        peerConnection?.addIceCandidate(candidate)
                    } else {
                        queuedRemoteCandidates.add(candidate)
                    }
                }
                //once we have received data we have to clear out the data
                sendSignallingMessage(mapOf("iceAnswer" to null))
            }
        }
        if (isOfferer && data["iceOffer"] != null) {
            executor.execute {
                val cMDataList = data["iceOffer"] as List<*>
                cMDataList.forEach { map ->

                    val cData = map as HashMap<*, *>
                    val candidate = IceCandidate(
                        /* sdpMid = */ cData["sdpMid"].toString(),
                        /* sdpMLineIndex = */ cData["sdpMLineIndex"].toString().toInt(),
                        /* sdp = */ cData["candidate"].toString()

                    )

                    //check remote description is set or not
                    if (remoteDescriptionSet) {
                        peerConnection?.addIceCandidate(candidate)
                    } else {
                        queuedRemoteCandidates.add(candidate)
                    }
                }
                //once we have received data we have to clear out the data
                sendSignallingMessage(mapOf("iceOffer" to null))
            }
        }
        if (isOfferer.not() && data["sdpOffer"] != null) {
            executor.execute {
                val offerSdp = data["sdpOffer"] as String
                val offer = SessionDescription(SessionDescription.Type.OFFER, offerSdp)
                sendSignallingMessage(mapOf("sdpOffer" to null))
                peerConnection?.setRemoteDescription(sdpObserver, offer)
                createAnswer()

            }
        }

        //completion of communication part
        if (isOfferer && data["sdpAnswer"] != null) {
            executor.execute {
                val answerSdp = data["sdpAnswer"] as String
                val answer = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                sendSignallingMessage(mapOf("sdpAnswer" to null))
                peerConnection?.setRemoteDescription(sdpObserver, answer)


            }
        }
    }

    fun setupFirebaseListener() {
        Timber.d("setupFirebaseListener() ::")
        val signallingRef = firestore.collection("rooms").document(roomID)
        signallingRef.addSnapshotListener { value, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            value?.data?.let {
                handleSignallingMessages(it)
            }
        }
    }

    fun createPeerConnection() {
        Timber.d("createPeerConnection() :: ")

        //using local stun servers for connection
        val iceServers = IceServer.builder(
            listOf(
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302"
            )
        )


        //creating peer connection with stun server
        val rtcConfig = PeerConnection.RTCConfiguration(listOf(iceServers.createIceServer()))

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {

                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                    Timber.d("CreatePeerConnection() :: onConnectionChange() :: $newState")

                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {

                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {

                }

                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {

                }

                override fun onIceCandidate(candidate: IceCandidate?) {
                    //send ice candidate to other peer
                    val key = if (isOfferer) "iceOffer" else "iceAnswer"

                    candidate?.let {
                        localCandidatesToShare.add(
                            mapOf(
                                "candidate" to it.sdp,
                                "sdpMid" to it.sdpMid,
                                "sdpMLineIndex" to it.sdpMLineIndex
                            )
                        )

                        //send data to signalling server and adding listener
                        sendSignallingMessage(
                            mapOf(
                                key to localCandidatesToShare
                            )
                        )
                    }
                }

                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {

                }

                override fun onAddStream(p0: MediaStream?) {

                }

                override fun onRemoveStream(p0: MediaStream?) {

                }

                override fun onDataChannel(p0: DataChannel?) {

                }

                override fun onRenegotiationNeeded() {

                }

                override fun onAddTrack(
                    p0: RtpReceiver?,
                    p1: Array<out MediaStream?>?
                ) {

                }

            })
    }


    fun createOffer() {
        Timber.d("createOffer() ::")
        peerConnection?.createOffer(
            sdpObserver, sdpMediaConstrains
        )
    }

    //calls first for one time
    LaunchedEffect(Unit) {
        checkRoomCapacity(onProceed = {
            //all business logic will be here
            //excuting one by one
            executor.execute {
                initializeWebRTC()
                createPeerConnection()
                setupFirebaseListener()
                if (isOfferer) {

                    createOffer()

                }
            }


        })
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ), title = {
                Text(text = "WebRTC Sample!", fontWeight = FontWeight.Bold)
            })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(eglBase.eglBaseContext,null)
                    }

                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(eglBase.eglBaseContext,null)
                    }

                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    }
}