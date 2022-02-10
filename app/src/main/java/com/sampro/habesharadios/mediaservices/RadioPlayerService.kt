package com.sampro.habesharadios.mediaservices

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import android.media.MediaRecorder
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.sampro.habesharadios.RadioPlayerActivity
import com.sampro.habesharadios.R
import com.sampro.habesharadios.utils.*
import java.io.File
import java.io.IOException
import java.util.*

class RadioPlayerService : LifecycleService() {

//    private val mBinder: IBinder = PlayerServiceBinder()
    var player: ExoPlayer? = null

    var recorder: MediaRecorder? = null

    private var fileRecordName: String = ""

    private var stationUrl: String? = null
    private var stationName = "Station Name"
    private var actions = mutableListOf("EXIT","CLOSE")

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    private var customActionReceiver: PlayerNotificationManager.CustomActionReceiver? = null

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus>()
    val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData


    inner class PlayerServiceBinder : Binder() {
        val service
            get() = this@RadioPlayerService
    }

//    companion object {
//
//        @MainThread
//        fun newIntent(context: Context, url: String? = null) = Intent(context, RadioPlayerService::class.java).apply {
//                putExtra(STATION_URL, url)
//        }
//
//    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)

//        handleIntent(intent)

        return PlayerServiceBinder()
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
            .build()
        player!!.addListener(PlayerEventListener())

        customActionReceiver = object : PlayerNotificationManager.CustomActionReceiver {
            override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
                val exitAction = Intent()
                exitAction.putExtra("action", actions[0])
                val closeAction = Intent()
                closeAction.putExtra("action", actions[1])
                return mutableMapOf(
                    Pair(actions[0], NotificationCompat.Action(R.drawable.ic_record, "Exit",
                        PendingIntent.getBroadcast(context, 0, Intent(exitAction).setPackage(context.packageName), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE))),
                    Pair(actions[1], NotificationCompat.Action(R.drawable.ic_record_stop, "Close",
                        PendingIntent.getBroadcast(context, 0, Intent(exitAction).setPackage(context.packageName), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)))
                )
            }

            override fun getCustomActions(player: Player): MutableList<String> {
                return actions
            }

            override fun onCustomAction(player: Player, action: String, intent: Intent) {
                Log.i("SAMUEL","Clicked First $action")
                when (action) {
                    "EXIT" -> {
                        Log.i("SAMUEL","Clicked Second $action")
//                            stopSelf()
                    }
                    "CLOSE" -> {
                        Log.i("SAMUEL","Clicked Second $action")
                    }
                }
            }

        }

        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return stationName
                }

//                @Nullable
//                override fun createCurrentContentIntent(player: Player): PendingIntent? {
//                    return null
//                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(this@RadioPlayerService, RadioPlayerActivity::class.java)
                    return PendingIntent.getActivity(this@RadioPlayerService,0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                }

//                @Nullable
//                override fun createCurrentContentIntent(player: Player): PendingIntent? = PendingIntent.getActivity(
//                    applicationContext,
//                    0,
//                    Intent(applicationContext, RadioPlayerActivity::class.java),
//                    PendingIntent.FLAG_UPDATE_CURRENT)


                @Nullable
                override fun getCurrentContentText(player: Player): CharSequence? {
                    return null
                }

                @Nullable
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
                    return getBitmapFromVectorDrawable(applicationContext, R.drawable.ic_baseline_radio)
                }

            })
//            .setCustomActionReceiver(customActionReceiver as PlayerNotificationManager.CustomActionReceiver)
            .setNotificationListener( object : PlayerNotificationManager.NotificationListener {

                override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    _playerStatusLiveData.value = PlayerStatus.Cancelled("Canceled")

                    stopSelf()
                }

            })
            .build()
        playerNotificationManager?.setUseStopAction(false)
        playerNotificationManager?.setUsePlayPauseActions(true)
        playerNotificationManager!!.setPlayer(player)
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG).apply {
            isActive = true
        }
        playerNotificationManager?.setMediaSessionToken(mediaSession!!.sessionToken)

        mediaSessionConnector = MediaSessionConnector(mediaSession!!).apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
                override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                    val bitmap = getBitmapFromVectorDrawable(applicationContext, R.drawable.ic_baseline_radio)
                    return MediaDescriptionCompat.Builder()
                        .setIconBitmap(bitmap)
                        .setTitle(stationName)
                        .build()
                }
            })
            
            setPlayer(player)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handleIntent(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        player?.release()
        stopRecording()

        super.onDestroy()
    }

    @MainThread
    private fun handleIntent(intent: Intent?) {
//        intent?.let {
//            stationUrl = intent.getStringExtra(STATION_URL)
//            stationName = intent.getStringExtra(STATION_NAME)
//            play(stationUrl!!)
//        }
        val bundle : Bundle? = intent?.extras
        stationName = bundle!!.getString(STATION_NAME).toString()
        stationUrl = bundle.getString(STATION_URL)
        play(stationUrl!!)
    }

    @MainThread
    fun play(url: String) {
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(url)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .build())
            .build()
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    @MainThread
    fun resume() {
        player?.playWhenReady = true
    }

    @MainThread
    fun pause() {
        player?.playWhenReady = false
    }

    @MainThread
    fun isPlaying() : Boolean {
        return player?.isPlaying == true
    }

    fun startRecording() {
        val audioCaptureDirectory = File(getExternalFilesDir(null), "/Recordings")
        if (!audioCaptureDirectory.exists()) {
            audioCaptureDirectory.mkdirs()
        }
        val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(Date())
        fileRecordName = "$stationName- $timestamp.aac"
        val outPutFile = "${audioCaptureDirectory.absolutePath}/$fileRecordName"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outPutFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("RECORDER", "prepare() failed")
            }
            start()
        }
    }

    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    @MainThread
    private fun getBitmapFromVectorDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap? {
        return ContextCompat.getDrawable(context, drawableId)?.let {
            val drawable = DrawableCompat.wrap(it).mutate()

            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        }
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onIsLoadingChanged(isLoading: Boolean) {
            if (isLoading) {
                _playerStatusLiveData.value = PlayerStatus.Loading("Loading")
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                _playerStatusLiveData.value = PlayerStatus.Playing("Playing")
            } else if (!isPlaying) {
                _playerStatusLiveData.value = PlayerStatus.Paused("Paused")
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                if (!player!!.playWhenReady) {
                    _playerStatusLiveData.value = PlayerStatus.Paused("Paused")
                }
            } else if (playbackState == Player.STATE_ENDED) {
                _playerStatusLiveData.value = PlayerStatus.Ended("Ended")
            }
            if (playbackState == Player.COMMAND_PLAY_PAUSE) {
                if (!player!!.isPlaying) {
                    _playerStatusLiveData.value = PlayerStatus.Paused("Paused")
                } else if (player!!.isPlaying) {
                    _playerStatusLiveData.value = PlayerStatus.Playing("Playing")
                }
            }
            if (playbackState == Player.STATE_BUFFERING) {
                _playerStatusLiveData.value = PlayerStatus.Loading("Loading")
            } else if (player!!.isPlaying) {
                _playerStatusLiveData.value = PlayerStatus.Playing("Playing")
            }
        }
    }

}