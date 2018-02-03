package com.ware.soundloadie.AudioBox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.ware.soundloadie.Containers.Charts
import com.ware.soundloadie.R
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import java.io.IOException
import java.util.ArrayList


class MediaPlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private var mediaPlayer: MediaPlayer? = null

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //Used to pause/resume MediaPlayer
    private var resumePosition: Int = 0

    //AudioFocus
    private var audioManager: AudioManager? = null

    // Binder given to clients
    private val iBinder = LocalBinder()

    //List of available Video files
    private var audioList: ArrayList<Audio>? = null
    private var audioIndex = -1
    private var activeAudio: Audio? = null
    internal var counts = 0


    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null


    /**
     * Service lifecycle methods
     */
    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Video to play -- BroadcastReceiver
        register_playNewAudio()

        register_playNextAudio()
        register_playPrevAudio()
        register_playAudio()
        register_pauseAudio()
        register_progressAudio()
        register_seekAudio()
        register_isPlaying()
        register_destroyPlayer()
    }

    //The system calls this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {


            try {

                //Load data from SharedPreferences
                val storage = StorageUtil(applicationContext)
                audioList = storage.loadAudio()
                audioIndex = storage.loadAudioIndex()

                if (audioIndex != -1 && audioIndex < audioList!!.size) {
                    //index is in a valid range
                    activeAudio = audioList!![audioIndex]
                } else {
                    stopSelf()
                }
            } catch (e: NullPointerException) {
                stopSelf()
            }

            //Request audio focus
            if (requestAudioFocus() == false) {
                //Could not gain focus
                stopSelf()
            }

            if (mediaSessionManager == null) {
                try {
                    initMediaSession()
                    initMediaPlayer()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    stopSelf()
                }

                buildNotification(PlaybackStatus.PLAYING)
            }

            //Handle Intent action from MediaSession.TransportControls
            handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer!!.release()

        }
        removeAudioFocus()
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        removeNotification()
        sendMessage("Destroy")

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewAudio)
        unregisterReceiver(playNextAudio)
        unregisterReceiver(playPrevAudio)
        unregisterReceiver(playAudio)
        unregisterReceiver(pauseAudio)
        unregisterReceiver(progressAudio)
        unregisterReceiver(seekAudio)
        unregisterReceiver(isPlaying)
        unregisterReceiver(destroyPlayer)

        //clear cached playlist
        StorageUtil(applicationContext).clearCachedAudioPlaylist()
        stopSelf()
    }


    /**
     * Service Binder
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods

    }


    /**
     * MediaPlayer callback methods
     */
    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onCompletion(mp: MediaPlayer) {
        //Invoked when playback of a media source has completed.
        skipToNext()
        updateMetaData()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra)
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra)
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra)
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        sendMessage("playing")
        sendMessage("duration" + mediaPlayer!!.duration)
        playMedia()
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onAudioFocusChange(focusState: Int) {

        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
        }
    }


    /**
     * AudioFocus
     */
    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true
        }
        //Could not gain focus
        return false
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!.abandonAudioFocus(this)
    }


    /**
     * MediaPlayer actions
     */
    private fun initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = MediaPlayer()//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        mediaPlayer!!.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()


        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            // Set the data source to the mediaFile location
            mediaPlayer!!.setDataSource(activeAudio!!.data)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }

        mediaPlayer!!.prepareAsync()
    }

    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
            sendMessage("pause")

        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
            sendMessage("playing")

        }
    }

    private fun skipToNext() {

        if (audioIndex == audioList!!.size - 1) {
            //if last in playlist
            audioIndex = 0
            activeAudio = audioList!![audioIndex]
            sendMessage("skip" + audioIndex)
        } else {
            //get next in playlist
            activeAudio = audioList!![++audioIndex]
            sendMessage("skip" + audioIndex)

        }

        //Update stored index
        StorageUtil(applicationContext).storeAudioIndex(audioIndex)

        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }

    private fun skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList!!.size - 1
            activeAudio = audioList!![audioIndex]
            sendMessage("skip" + audioIndex)

        } else {
            //get previous in playlist
            activeAudio = audioList!![--audioIndex]
            sendMessage("skip" + audioIndex)

        }

        //Update stored index
        StorageUtil(applicationContext).storeAudioIndex(audioIndex)

        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }


    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    /**
     * Handle PhoneState changes
     */
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                //if at least one call exists or the phone is ringing
                //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE)
    }

    /**
     * MediaSession and Notification actions
     */
    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return  //mediaSessionManager exists

        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession!!.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession!!.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()

                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()

                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
                stopForeground(false)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()

                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()

                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                sendMessage("Destroy")
                //Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(resources,
                R.drawable.large_audio_icon) //replace with medias albumArt
        // Update the current metadata
        mediaSession!!.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio!!.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio!!.album)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio!!.title)
                .build())
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {

        /**
         * Notification actions -> playbackAction()
         * 0 -> Play
         * 1 -> Pause
         * 2 -> Next track
         * 3 -> Previous track
         */

        var notificationAction = android.R.drawable.ic_media_pause//needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        val largeIcon = BitmapFactory.decodeResource(resources,
                R.drawable.large_audio_icon) //replace with your own image

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var notificationBuilder :NotificationCompat.Builder? = null
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT;
                val notificationChannel = NotificationChannel("ID", "Name", importance);
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationBuilder = NotificationCompat.Builder(applicationContext, notificationChannel.getId());
            } else {
                    notificationBuilder = NotificationCompat.Builder(applicationContext);
            }
                // Hide the timestamp
        notificationBuilder.setShowWhen(false)
                // Set the Notification style
                .setStyle(MediaStyle()
                        .setMediaSession(mediaSession!!.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(null)
                )

                .setColor(ContextCompat.getColor(applicationContext,R.color.colorAccent))
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_headset)
                .setContentText(activeAudio!!.artist)
                .setDeleteIntent(createOnDismissedIntent(applicationContext, NOTIFICATION_ID))
                .setContentTitle(activeAudio!!.album)
                .setContentInfo(activeAudio!!.title)
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2)) as NotificationCompat.Builder




        startForeground(NOTIFICATION_ID, notificationBuilder!!.build())

    }


    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return

        val actionString = playbackAction.action
        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls!!.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls!!.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls!!.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls!!.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls!!.stop()
        }
    }


    /**
     * Play new Video
     */
    private val playNewAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            audioList = StorageUtil(applicationContext).loadAudio()
            audioIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (audioIndex != -1 && audioIndex < audioList!!.size) {
                //index is in a valid range
                activeAudio = audioList!![audioIndex]
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Video
            stopMedia()
            mediaPlayer!!.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
            sendMessage("nue"+audioIndex)
        }
    }



    //================================================



    private val destroyPlayer = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    private fun register_destroyPlayer() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Player.Broadcast_DESTROY_PLAYER)
        registerReceiver(destroyPlayer, filter)
    }




    //===================================================







    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Charts.Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }


    private val playNextAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            skipToNext()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playNextAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Player.Broadcast_PLAY_NEXT_AUDIO)
        registerReceiver(playNextAudio, filter)
    }


    private val playPrevAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            skipToPrevious()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playPrevAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Player.Broadcast_PLAY_PREV_AUDIO)
        registerReceiver(playPrevAudio, filter)
    }


    private val playAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            resumeMedia()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Player.Broadcast_PLAY_AUDIO)
        registerReceiver(playAudio, filter)
    }


    private val pauseAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun register_pauseAudio() {
        val filter = IntentFilter(Player.Broadcast_PAUSE_AUDIO)
        registerReceiver(pauseAudio, filter)
    }


    private val isPlaying = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (mediaPlayer!!.isPlaying) {

                sendMessage("playing")

            }


        }
    }

    private fun register_isPlaying() {
        val filter = IntentFilter(Player.Broadcast_IS_PLAYING)
        registerReceiver(isPlaying, filter)
    }


    private val progressAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (!mediaPlayer!!.isPlaying) {

            } else {

                val numero = mediaPlayer!!.currentPosition
                Updatebar(numero)
            }

        }
    }


    private val seekAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val seeker = intent.getIntExtra("skipto", 0)
            if (!mediaPlayer!!.isPlaying) {

            } else {

                pauseMedia()
                if (mediaPlayer != null) {
                    val currentPosition = mediaPlayer!!.currentPosition
                    mediaPlayer!!.seekTo(seeker)
                    resumePosition = seeker

                    resumeMedia()
                    buildNotification(PlaybackStatus.PLAYING)
                }

            }

        }
    }

    private fun register_seekAudio() {
        val filter = IntentFilter(Player.Broadcast_SEEK_AUDIO)
        registerReceiver(seekAudio, filter)
    }


    private fun register_progressAudio() {
        val filter = IntentFilter(Player.Broadcast_PROGRESS_AUDIO)
        registerReceiver(progressAudio, filter)
    }


    private fun sendMessage(baba: String) {
        // The string "my-integer" will be used to filer the intent
        val intent = Intent("message")
        // Adding some data
        intent.putExtra("messages", baba)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun Updatebar(bar: Int) {
        sendMessage("progress" + bar)
        sendMessage("duration" + mediaPlayer!!.duration)


    }

    companion object {


        val ACTION_PLAY = "com.naira.soundloadd.ACTION_PLAY"
        val ACTION_PAUSE = "com.naira.soundloadd.ACTION_PAUSE"
        val ACTION_PREVIOUS = "com.naira.soundloadd.ACTION_PREVIOUS"
        val ACTION_NEXT = "com.naira.soundloadd.ACTION_NEXT"
        val ACTION_STOP = "com.naira.soundloadd.ACTION_STOP"

        //AudioPlayer notification ID
        private val NOTIFICATION_ID = 101
    }


    private fun createOnDismissedIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        intent.putExtra("com.naira.soundloadd.notificationId", notificationId)

        val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, notificationId, intent, 0)
        return pendingIntent
    }



}
