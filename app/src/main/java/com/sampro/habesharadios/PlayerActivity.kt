package com.sampro.habesharadios

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.exoplayer2.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception
import java.util.*
import com.sampro.habesharadios.mediaservices.PlayerService
import com.sampro.habesharadios.utils.STATION_NAME
import com.sampro.habesharadios.utils.STATION_URL
import com.sampro.habesharadios.utils.isServiceRunning


class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RadioPlayer"
    }

    private var player: ExoPlayer? = null

    var playerService: PlayerService? = null

    var pIsBound: Boolean? = null

    var stationName: String? = null
    var url: String? = null

    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton

    private lateinit var seekBar: SeekBar

    private lateinit var ivPlayer: ImageView

    private lateinit var tvPlayTime: TextView

    private lateinit var progressBarLoading: ProgressBar

    private var playTime = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        fabPlay = findViewById(R.id.fabPlay)
        fabRecord = findViewById(R.id.fabRecord)
        fabStop = findViewById(R.id.fabStop)

        seekBar = findViewById(R.id.seekBar)

        ivPlayer = findViewById(R.id.ivPlayer)

        tvPlayTime = findViewById(R.id.tvPlayTime)

        progressBarLoading = findViewById(R.id.progressBarLoading)

        if (!applicationContext.isServiceRunning(PlayerService::class.java.name)) {
            val bundle : Bundle? = intent.extras
            stationName = bundle!!.getString("name")
            url = bundle.getString("url")
        }

        setStationImage(stationName.toString())

        progressBarLoading.visibility = View.GONE

//        playRadio(url.toString())

//        object : CountDownTimer(50000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                // Used for formatting digit to be in 2 digits only
//                val f: NumberFormat = DecimalFormat("00")
//                val hour = millisUntilFinished / 3600000 % 24
//                val min = millisUntilFinished / 60000 % 60
//                val sec = millisUntilFinished / 1000 % 60
//                tvPlayTime.text = f.format(hour).toString() + ":" + f.format(min) + ":" + f.format(sec)
//            }
//
//            // When the task is over it will print 00:00:00 there
//            override fun onFinish() {
//                tvPlayTime.text = "00:00:00"
//            }
//        }.start()
//

        fabPlay.setOnClickListener {

            if (playerService == null) {
                bindToPlayerService()
            } else {
                if (playerService!!.isPlaying()) {
                    playerService?.pause()
                    fabPlay.setImageResource(R.drawable.ic_play_arrow)
                    tvPlayTime.text = "Paused"
                } else if (!playerService!!.isPlaying()) {
                    playerService?.resume()
                    fabPlay.setImageResource(R.drawable.ic_pause)
                    tvPlayTime.text = "Playing"
                }
            }

        }

        fabRecord.setOnClickListener {
//            if (player !== null) {
//                player?.pause()
//                fabPlay.setImageResource(R.drawable.ic_play_arrow)
//            }
        }

        fabStop.setOnClickListener {
//            if (player !== null) {
//                player?.stop()
//                player?.release()
//                player = null
//                fabPlay.setImageResource(R.drawable.ic_play_arrow)
//                tvPlayTime.text = "Stopped."
//            }
            stopPlayerService()
            fabPlay.setImageResource(R.drawable.ic_play_arrow)
            tvPlayTime.text = "Stopped."
        }

//        startPlayerService()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.PlayerServiceBinder
            playerService = binder.service
            pIsBound = true
            playerService?.playerStatusLiveData?.observe(this@PlayerActivity, {
                when (it.playerStatus) {
                    "Loading" -> {
                        fabPlay.setImageResource(R.drawable.ic_pause)
                        tvPlayTime.text = "Loading..."
                    }
                    "Paused" -> {
                        fabPlay.setImageResource(R.drawable.ic_play_arrow)
                        tvPlayTime.text = "Paused"
                    }
                    "Ended" -> {
                        fabPlay.setImageResource(R.drawable.ic_play_arrow)
                        tvPlayTime.text = "Stopped"
                    }
                    "Playing" -> {
                        fabPlay.setImageResource(R.drawable.ic_pause)
                        tvPlayTime.text = "Playing"
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pIsBound = false
            playerService = null
        }
    }

    private fun startPlayerService() {
//        PlayerService.newIntent(this, url).also { intent ->
//            startService(intent)
//        }
    }


    private fun bindToPlayerService() {
        if (playerService == null) {
            val intent = Intent(this, PlayerService::class.java)
            intent.putExtra(STATION_URL, url)
            intent.putExtra(STATION_NAME, stationName)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//            PlayerService.newIntent(this).also { intent ->
//                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//            }
            startService(intent)
        }
    }

    private fun unbindPlayerService() {
        if (playerService != null) {
            unbindService(serviceConnection)

            playerService = null
        }
    }

    private fun stopPlayerService() {
//        playerService?.pause()

        unbindPlayerService()
        stopService(Intent(this, PlayerService::class.java))

        playerService = null
    }


    override fun onStart() {
        super.onStart()

        if (!applicationContext.isServiceRunning(PlayerService::class.java.name)) {
            bindToPlayerService()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayerService()
    }

//    private fun addPlayerEventListener() {
//        player!!.addListener(object : Player.Listener {
//            override fun onTimelineChanged(timeline: Timeline, reason: Int) {}
//            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}
//            @SuppressLint("SetTextI18n")
//            override fun onIsLoadingChanged(isLoading: Boolean) {
//                if (isLoading) {
//                    tvPlayTime.text = "Loading..."
//                    progressBarLoading.visibility = View.VISIBLE
//                }
//            }
//            @SuppressLint("SetTextI18n")
//            override fun onPlaybackStateChanged(playbackState: Int) {
//                if (playbackState == Player.STATE_BUFFERING) {
//                    tvPlayTime.text = "Loading..."
//                    progressBarLoading.visibility = View.VISIBLE
//                } else if (player!!.isPlaying) {
//                    tvPlayTime.text = "Playing."
//                    progressBarLoading.visibility = View.GONE
//                }
//            }
//            override fun onPlayerError(error: PlaybackException) {}
//            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {}
//            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
//        })
//    }

    private fun playRadio(url: String) {
//        if (player == null) {
//            player = ExoPlayer.Builder(this)
//                .setMediaSourceFactory(
//                    DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
//                .build()
//            // Per MediaItem settings.
//            val mediaItem: MediaItem = MediaItem.Builder()
//                .setUri(url)
//                .setLiveConfiguration(
//                    LiveConfiguration.Builder()
//                        .setMaxPlaybackSpeed(1.02f)
//                        .build())
//                .build()
//            player?.setMediaItem(mediaItem)
//            player?.prepare()
//            addPlayerEventListener()
//        }
//        player?.play()
//        fabPlay.setImageResource(R.drawable.ic_pause)
    }

    private fun setStationImage(station: String) {
        when (station) {
            "Afro FM 105.3" -> {
                ivPlayer.setImageResource(R.drawable.afro_fm_01)
            }
            "Sheger FM 102.1" -> {
                ivPlayer.setImageResource(R.drawable.sheger_fm_01)
            }
            "Ahadu FM 94.3" -> {
                ivPlayer.setImageResource(R.drawable.ahadu_fm_01)
            }
            "Awash FM 90.7" -> {
                ivPlayer.setImageResource(R.drawable.awash_fm_01)
            }
            "Bisrat FM 101.1" -> {
                ivPlayer.setImageResource(R.drawable.bisrat_fm_01)
            }
            "EBC FM Addis 97.1" -> {
                ivPlayer.setImageResource(R.drawable.ebc_fm_addis_01)
            }
            "Ethio FM 107.8" -> {
                ivPlayer.setImageResource(R.drawable.ethio_fm_01)
            }
        }
    }

    /*private fun controlRadio(radioUri: String){
        fabPlay.setOnClickListener {
            if (mp == null){
                mp = MediaPlayer.create(this, Uri.parse("http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one"))
//                Log.d("PlayerActivity", "ID: ${mp!!.audioSessionId}")

//                initialiseSeekBar()
            }
            mp?.start()
//            Log.d("PlayerActivity", "Duration: ${mp!!.duration/1000} seconds")
        }

        fabPause.setOnClickListener {
            if (mp !== null) mp?.pause()
//            Log.d("PlayerActivity", "Paused at: ${mp!!.currentPosition/1000} seconds")
        }

        fabStop.setOnClickListener {
            if (mp !== null) {
                mp?.stop()
                mp?.reset()
                mp?.release()
                mp = null
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mp?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

    }*/

    private fun initialiseSeekBar() {
        seekBar.max = 5000

        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run() {
                try {
                    seekBar.progress = player?.currentPosition!!.toInt()
                    handler.postDelayed(this,1000)
                } catch (e: Exception) {
                    seekBar.progress = 0
                }
            }
        }, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopPlayerService()
    }
}