package com.sampro.habesharadios

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.exoplayer2.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception
import java.util.*
import com.sampro.habesharadios.mediaservices.PlayerService
import com.sampro.habesharadios.utils.RequestPermissions
import com.sampro.habesharadios.utils.STATION_NAME
import com.sampro.habesharadios.utils.STATION_URL
import com.sampro.habesharadios.utils.isServiceRunning


class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RadioPlayer"
    }

    private lateinit var requestPermissions: RequestPermissions

    private var player: ExoPlayer? = null

    var playerService: PlayerService? = null

    var pIsBound: Boolean? = null

    var stationName: String? = null
    var url: String? = null

    private var isRecordClicked = false

    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton

    private lateinit var clLoading: ConstraintLayout
    private lateinit var clRecordingStatus: ConstraintLayout

    private lateinit var seekBar: SeekBar

    private lateinit var ivPlayer: ImageView
    private lateinit var ivRecordingStatus: ImageView

    private lateinit var tvPlayTime: TextView

    private lateinit var progressBarLoading: ProgressBar

    private var playTime = 0

    private var startAnimation: Boolean = false
    private var isRecording: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        setupViewControls()

        if (!applicationContext.isServiceRunning(PlayerService::class.java.name)) {
            val bundle : Bundle? = intent.extras
            stationName = bundle!!.getString("name")
            url = bundle.getString("url")
        } else {
            finish()
        }

        setStationImage(stationName.toString())

        requestPermissions = RequestPermissions(this, this)

//        progressBarLoading.visibility = View.GONE

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

//        checkPermissions()

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
//            checkPermissions()
            val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fade_out_color)

            if (playerService!!.isPlaying() && !isRecording) {
                playerService?.startRecording()
                ivRecordingStatus.startAnimation(anim)
                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                clRecordingStatus.visibility = View.VISIBLE
                isRecording = true
                startAnimation = true
            } else if (isRecording) {
                playerService?.stopRecording()
                ivRecordingStatus.clearAnimation()
                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                clRecordingStatus.visibility = View.GONE
                isRecording = false
                startAnimation = false
            }
//            if (!startAnimation) {
//                ivRecordingStatus.startAnimation(anim)
//                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
//                clRecordingStatus.visibility = View.VISIBLE
//                startAnimation = true
//            } else {
//                ivRecordingStatus.clearAnimation()
//                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
//                clRecordingStatus.visibility = View.GONE
//                startAnimation = false
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

    private fun setupViewControls() {
        fabPlay = findViewById(R.id.fabPlay)
        fabRecord = findViewById(R.id.fabRecord)
        fabStop = findViewById(R.id.fabStop)

        seekBar = findViewById(R.id.seekBar)

        clLoading = findViewById(R.id.clLoading)
        clRecordingStatus = findViewById(R.id.clRecordingStatus)

        ivPlayer = findViewById(R.id.ivPlayer)
        ivRecordingStatus = findViewById(R.id.ivRecordingStatus)

        tvPlayTime = findViewById(R.id.tvPlayTime)

        progressBarLoading = findViewById(R.id.progressBarLoading)
    }

    private fun showPermissionDeniedDialog() {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .cancelable(false)
            .cancelOnTouchOutside(false)
            .cornerRadius(14f)
            .customView(R.layout.permission_denied_dialog_layout)

        dialog.findViewById<Button>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            checkPermissions()
        }

        dialog.findViewById<Button>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
//            finish()
        }

        dialog.show()

    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (!requestPermissions.hasWriteExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
                requestPermissions.requestPermissions()
            }
        } else {
            if (!requestPermissions.hasReadExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
                requestPermissions.requestPermissions()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (!requestPermissions.hasWriteExternalStoragePermission() || !requestPermissions.hasReadExternalStoragePermission()
                || !requestPermissions.hasRecordAudioPermission()) {
                showPermissionDeniedDialog()
//                Toast.makeText(this, "Storage Read Write Permission Denied!", Toast.LENGTH_SHORT).show()
                Log.i(TAG,"Storage Read Write and Record Audio Permission Denied!")
            } else {
                Log.i(TAG,"Storage Read Write and Record Audio Permission Accepted!")
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!requestPermissions.hasReadExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
//                Toast.makeText(this, "Storage Read Write Permission Denied!", Toast.LENGTH_SHORT).show()
                showPermissionDeniedDialog()
                Log.i(TAG,"Storage Read Write and Record Audio Permission Denied!")
            } else {
                Log.i(TAG,"Storage Read Write and Record Audio Permission Accepted!")
            }
        }

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
//                        progressBarLoading.visibility = View.VISIBLE
                        clLoading.visibility = View.VISIBLE
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
//                        progressBarLoading.visibility = View.GONE
                        clLoading.visibility = View.GONE
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pIsBound = false
            playerService = null
        }
    }

//    private fun startPlayerService() {
//        PlayerService.newIntent(this, url).also { intent ->
//            startService(intent)
//        }
//    }


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
            "EBC FM 104.7" -> {
                ivPlayer.setImageResource(R.drawable.ebc_radio_fm_01)
            }
            "OBN Radio" -> {
                ivPlayer.setImageResource(R.drawable.obn_radio_01)
            }
            else -> {
                ivPlayer.setImageResource(R.drawable.ic_baseline_radio)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayerService()
    }

    override fun onBackPressed() {
        super.onBackPressed()
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

//    private fun initialiseSeekBar() {
//        seekBar.max = 5000
//
//        val handler = Handler()
//        handler.postDelayed(object: Runnable {
//            override fun run() {
//                try {
//                    seekBar.progress = player?.currentPosition!!.toInt()
//                    handler.postDelayed(this,1000)
//                } catch (e: Exception) {
//                    seekBar.progress = 0
//                }
//            }
//        }, 0)
//    }


}