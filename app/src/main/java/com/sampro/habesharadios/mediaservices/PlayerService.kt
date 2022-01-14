package com.sampro.habesharadios.mediaservices

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.Nullable
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
import com.sampro.habesharadios.PlayerActivity
import com.sampro.habesharadios.R
import com.sampro.habesharadios.utils.*

class PlayerService : LifecycleService() {

//    private val mBinder: IBinder = PlayerServiceBinder()
    var player: ExoPlayer? = null

    private var stationUrl: String? = null
    private var stationName = "Station Name"

    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private val _playerStatusLiveData = MutableLiveData<PlayerStatus>()
    val playerStatusLiveData: LiveData<PlayerStatus>
        get() = _playerStatusLiveData


    inner class PlayerServiceBinder : Binder() {
        val service
            get() = this@PlayerService
    }

//    companion object {
//
//        @MainThread
//        fun newIntent(context: Context, url: String? = null) = Intent(context, PlayerService::class.java).apply {
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

        playerNotificationManager = PlayerNotificationManager.Builder(
            applicationContext,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return stationName
                }

                @Nullable
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return null
                }

//                @Nullable
//                override fun createCurrentContentIntent(player: Player): PendingIntent? = PendingIntent.getActivity(
//                    applicationContext,
//                    0,
//                    Intent(applicationContext, PlayerActivity::class.java),
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
        playerNotificationManager?.setUsePlayPauseActions(false)
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

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                if (!player!!.playWhenReady) {
                    _playerStatusLiveData.value = PlayerStatus.Paused("Paused")
                }
            } else if (playbackState == Player.STATE_ENDED) {
                _playerStatusLiveData.value = PlayerStatus.Ended("Ended")
            }
            if (playbackState == Player.STATE_BUFFERING) {
                _playerStatusLiveData.value = PlayerStatus.Loading("Loading")
            } else if (player!!.isPlaying) {
                _playerStatusLiveData.value = PlayerStatus.Playing("Playing")
            }
        }
    }

}