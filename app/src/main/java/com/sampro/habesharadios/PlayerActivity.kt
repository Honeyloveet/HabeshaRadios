package com.sampro.habesharadios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.LiveConfiguration
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton

    private lateinit var seekBar: SeekBar

    private lateinit var ivPlayer: ImageView

    private lateinit var tvPlayTime: TextView

    private var playTime = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        fabPlay = findViewById(R.id.fabPlay)
        fabRecord = findViewById(R.id.fabRecord)
        fabStop = findViewById(R.id.fabStop)

        seekBar = findViewById(R.id.seekBar)

        ivPlayer = findViewById(R.id.ivPlayer)

        tvPlayTime = findViewById(R.id.tvPlayTime)

        val bundle : Bundle? = intent.extras
        val stationName = bundle!!.getString("name")
        val url = bundle.getString("url")

        setStationImage(stationName.toString())

        playRadio(url.toString())

        fabPlay.setOnClickListener {
            if (player == null) {
                player = ExoPlayer.Builder(this)
                    .setMediaSourceFactory(
                        DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                    .build()
                Log.d("honey", url.toString())
                // Per MediaItem settings.
                val mediaItem: MediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setLiveConfiguration(
                        LiveConfiguration.Builder()
                            .setMaxPlaybackSpeed(1.02f)
                            .build())
                    .build()
                player?.setMediaItem(mediaItem)
                player?.prepare()
            }
            if (!player!!.isPlaying) {
                player?.play()
                fabPlay.setImageResource(R.drawable.ic_pause)
            } else if (player!!.isPlaying) {
                player?.pause()
                fabPlay.setImageResource(R.drawable.ic_play_arrow)
            }
        }

        fabRecord.setOnClickListener {
//            if (player !== null) {
//                player?.pause()
//                fabPlay.setImageResource(R.drawable.ic_play_arrow)
//            }
        }

        fabStop.setOnClickListener {
            if (player !== null) {
                player?.stop()
                player?.release()
                player = null
                fabPlay.setImageResource(R.drawable.ic_play_arrow)
            }
        }
    }

    private fun playRadio(url: String) {
        if (player == null) {
            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                .build()
            Log.d("honey", url.toString())
            // Per MediaItem settings.
            val mediaItem: MediaItem = MediaItem.Builder()
                .setUri(url)
                .setLiveConfiguration(
                    LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.02f)
                        .build())
                .build()
            player?.setMediaItem(mediaItem)
            player?.prepare()
        }
        player?.play()
        fabPlay.setImageResource(R.drawable.ic_pause)
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
        player?.release()
    }
}