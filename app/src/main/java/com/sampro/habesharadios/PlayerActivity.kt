package com.sampro.habesharadios

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sampro.habesharadios.utils.RadioStations
import java.lang.Exception
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.LiveConfiguration
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

class PlayerActivity : AppCompatActivity() {

//    private val player = ExoPlayer.Builder(this)
//        .setMediaSourceFactory(
//            DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
//        .build()
    private lateinit var player: ExoPlayer

    private var mp: MediaPlayer? = null

    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabPause: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton

    private lateinit var seekBar: SeekBar

    private lateinit var ivPlayer: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        fabPlay = findViewById(R.id.fabPlay)
        fabPause = findViewById(R.id.fabPause)
        fabStop = findViewById(R.id.fabStop)

        seekBar = findViewById(R.id.seekBar)

        ivPlayer = findViewById(R.id.ivPlayer)

        val bundle : Bundle? = intent.extras
        val station = bundle!!.getString("station")

        setStationImage(station.toString())
//        val player = ExoPlayer.Builder(this).build()
//        // Build the media item.
//        val mediaItem: MediaItem = MediaItem.fromUri("http://stream.live.vc.bbcmedia.co.uk/bbc_radio_one")
//        // Set the media item to be played.
//        player.setMediaItem(mediaItem)
//        // Prepare the player.
//        player.prepare()
//        // Start the playback.
//        // Start the playback.
////        player.play()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
            .build()
        Log.d("honey", RadioStations.stations[station].toString())
        // Per MediaItem settings.
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(RadioStations.stations[station])
            .setLiveConfiguration(
                LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .build())
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
//        player.play()

        fabPlay.setOnClickListener {
            if (!player.isPlaying) {
                player.play()
            } else {
                player.pause()
            }
        }

//        controlSound(currentSong[0])
//        controlRadio(RadioStations.stations["Afro FM"].toString())

    }

    private fun setStationImage(station: String) {
        when (station) {
            "Afro FM" -> {
                ivPlayer.setImageResource(R.drawable.afro_fm_01)
            }
            "Sheger FM" -> {
                ivPlayer.setImageResource(R.drawable.sheger_fm_01)
            }
            "Ahadu FM" -> {
                ivPlayer.setImageResource(R.drawable.ahadu_fm_01)
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
        seekBar.max = mp!!.duration

        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run() {
                try {
                    seekBar.progress = mp!!.currentPosition
                    handler.postDelayed(this,1000)
                } catch (e: Exception) {
                    seekBar.progress = 0
                }
            }
        }, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        player.release()
    }
}