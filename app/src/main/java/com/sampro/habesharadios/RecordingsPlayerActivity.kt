package com.sampro.habesharadios

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.sampro.habesharadios.model.StationsModelParcelable

class RecordingsPlayerActivity : AppCompatActivity(), Player.Listener {

    private lateinit var recording: StationsModelParcelable

    private lateinit var player: Player
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings_player)

        progressBar = findViewById(R.id.progressBar)
        tvTitle = findViewById(R.id.tvTitle)

        setupPlayer()

        recording = intent.getParcelableExtra("stationToPlay")!!
        val recordingUri = Uri.parse(recording.uri)
        Log.d("SAMPROOOO","${recording.name} $recordingUri")
        playerView.defaultArtwork = getStationArtWork(recording.name.toString())
        tvTitle.text = recording.name
        addMediaFiles(recordingUri)

    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView = findViewById(R.id.playerView)
        playerView.player = player
        player.addListener(this)
    }

    private fun addMediaFiles(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.addMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private fun getStationArtWork(name: String) : Drawable? {
        val x : Drawable?
        when (name) {
            "Afro FM 105.3" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.afro_fm_01, null)
            }
            "Sheger FM 102.1" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.sheger_fm_01, null)
            }
            "Ahadu FM 94.3" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.ahadu_fm_01, null)
            }
            "Awash FM 90.7" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.awash_fm_01, null)
            }
            "Bisrat FM 101.1" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.bisrat_fm_01, null)
            }
            "EBC FM Addis 97.1" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.ebc_fm_addis_01, null)
            }
            "Ethio FM 107.8" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.ethio_fm_01, null)
            }
            "EBC FM 104.7" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.ebc_radio_fm_01, null)
            }
            "OBN Radio" -> {
                x = ResourcesCompat.getDrawable(resources, R.drawable.obn_radio_01, null)
            }
            else -> x =  ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_radio, null)

        }
        return x
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        when (playbackState) {
            Player.STATE_BUFFERING -> {
                progressBar.visibility = View.VISIBLE
            }
            Player.STATE_READY -> {
                progressBar.visibility = View.INVISIBLE
            }
            Player.STATE_ENDED -> {

            }
            Player.STATE_IDLE -> {
                TODO()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}