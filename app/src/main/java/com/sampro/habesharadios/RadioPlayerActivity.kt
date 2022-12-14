package com.sampro.habesharadios

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.exoplayer2.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sampro.habesharadios.mediaservices.RadioPlayerService
import com.sampro.habesharadios.utils.RequestPermissions
import com.sampro.habesharadios.utils.STATION_NAME
import com.sampro.habesharadios.utils.STATION_URL
import com.sampro.habesharadios.utils.isServiceRunning
import java.util.*


class RadioPlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SAMPRO_ONE"
    }

    private lateinit var requestPermissions: RequestPermissions

    private var player: ExoPlayer? = null

    var radioPlayerService: RadioPlayerService? = null

    var pIsBound: Boolean? = null

    var stationName: String = "Habesha Radios"
    var url: String? = null

    private var isRecordClicked = false

    private lateinit var fabPlay: FloatingActionButton
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var fabStop: FloatingActionButton

    private lateinit var clLoading: ConstraintLayout
    private lateinit var clRecordingStatus: ConstraintLayout
    private lateinit var llAdHolder: LinearLayout

    private lateinit var seekBar: SeekBar

    private lateinit var ivPlayer: ImageView
    private lateinit var ivRecordingStatus: ImageView

    private lateinit var tvPlayTime: TextView

    private lateinit var progressBarLoading: ProgressBar

    private lateinit var template: TemplateView

    private var playTime = 0

    private var startAnimation: Boolean = false
    private var isRecording: Boolean = false
    private var isPlayingFirstTime: Boolean = true

    private lateinit var adLoader: AdLoader

    private lateinit var countDownTimer: CountDownTimer
    private var count = 0L
    private val start = 5_000L
    private var timer = start

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio_player)

        MobileAds.initialize(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        setupViewControls()

        if (!applicationContext.isServiceRunning(RadioPlayerService::class.java.name)) {
            val bundle : Bundle? = intent.extras
            stationName = bundle!!.getString("name").toString()
            url = bundle.getString("url")
        } else {
            finish()
        }

        supportActionBar?.title = stationName

        setStationImage(stationName)

        requestPermissions = RequestPermissions(this, this)

        adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->
                val styles = NativeTemplateStyle.Builder().build()
                template = findViewById(R.id.my_template)
                template.setStyles(styles)
                template.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    Toast.makeText(this@RadioPlayerActivity,"Native Ad Closed.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Native Ad Closed")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d(TAG, "Native Ad Load Failed.")
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Log.d(TAG, "Native Ad Opened")
                    template.setNativeAd(null)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "Native Ad Loaded")
                    llAdHolder.visibility = View.VISIBLE
                    template.setNativeAd(null)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

            })
            .build()

//        adLoader.loadAd(AdRequest.Builder().build())

        countDownTimer = object : CountDownTimer(timer, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count++
            }

            override fun onFinish() {
                Toast.makeText(this@RadioPlayerActivity,"Timer Finished at $count!", Toast.LENGTH_SHORT).show()
//                llAdHolder.visibility = View.VISIBLE
                adLoader.loadAd(AdRequest.Builder().build())
            }
        }

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

            Log.i("Honey","Play Clicked!!")
            if (radioPlayerService == null) {
                bindToPlayerService()
            } else {
                if (radioPlayerService!!.isPlaying()) {
                    radioPlayerService?.pause()
                    fabPlay.setImageResource(R.drawable.ic_play_arrow)
                    tvPlayTime.text = "Paused"
                } else if (!radioPlayerService!!.isPlaying()) {
                    radioPlayerService?.resume()
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
            val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fade_out_color)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (!requestPermissions.hasWriteExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
                    checkPermissions()
                } else {
                    if (radioPlayerService!!.isPlaying() && !isRecording) {
                        radioPlayerService?.startRecording()
                        ivRecordingStatus.startAnimation(anim)
                        fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                        clRecordingStatus.visibility = View.VISIBLE
                        isRecording = true
                        startAnimation = true
                    } else if (isRecording) {
                        radioPlayerService?.stopRecording()
                        ivRecordingStatus.clearAnimation()
                        fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                        clRecordingStatus.visibility = View.GONE
                        isRecording = false
                        startAnimation = false
                    }
                }
            } else {
                if (!requestPermissions.hasReadExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
                    checkPermissions()
                } else {
                    if (radioPlayerService!!.isPlaying() && !isRecording) {
                        radioPlayerService?.startRecording()
                        ivRecordingStatus.startAnimation(anim)
                        fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                        clRecordingStatus.visibility = View.VISIBLE
                        isRecording = true
                        startAnimation = true
                    } else if (isRecording) {
                        radioPlayerService?.stopRecording()
                        ivRecordingStatus.clearAnimation()
                        fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                        clRecordingStatus.visibility = View.GONE
                        isRecording = false
                        startAnimation = false
                    }
                }
            }
//            checkPermissions()
//            if (isRecording) {
//                radioPlayerService?.stopRecording()
//                ivRecordingStatus.clearAnimation()
//                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
//                clRecordingStatus.visibility = View.GONE
//                isRecording = false
//                startAnimation = false
//            }
/*            val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fade_out_color)

            if (radioPlayerService!!.isPlaying() && !isRecording) {
                radioPlayerService?.startRecording()
                ivRecordingStatus.startAnimation(anim)
                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                clRecordingStatus.visibility = View.VISIBLE
                isRecording = true
                startAnimation = true
            } else if (isRecording) {
                radioPlayerService?.stopRecording()
                ivRecordingStatus.clearAnimation()
                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                clRecordingStatus.visibility = View.GONE
                isRecording = false
                startAnimation = false
            }*/

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
            if (isRecording) {
                radioPlayerService?.stopRecording()
                ivRecordingStatus.clearAnimation()
                fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                clRecordingStatus.visibility = View.GONE
                isRecording = false
                startAnimation = false
            }
            stopPlayerService()
            fabPlay.setImageResource(R.drawable.ic_play_arrow)
            tvPlayTime.text = "Stopped."
            llAdHolder.visibility = View.GONE
        }

//        startPlayerService()
        if (!applicationContext.isServiceRunning(RadioPlayerService::class.java.name)) {
            bindToPlayerService()
        }
    }

    private fun setupViewControls() {
        fabPlay = findViewById(R.id.fabPlay)
        fabRecord = findViewById(R.id.fabRecord)
        fabStop = findViewById(R.id.fabStop)

        seekBar = findViewById(R.id.seekBar)

        clLoading = findViewById(R.id.clLoading)
        clRecordingStatus = findViewById(R.id.clRecordingStatus)
        llAdHolder = findViewById(R.id.llAdHolder)

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
                val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fade_out_color)

                if (radioPlayerService!!.isPlaying() && !isRecording) {
                    radioPlayerService?.startRecording()
                    ivRecordingStatus.startAnimation(anim)
                    fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                    clRecordingStatus.visibility = View.VISIBLE
                    isRecording = true
                    startAnimation = true
                }
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!requestPermissions.hasReadExternalStoragePermission() || !requestPermissions.hasRecordAudioPermission()) {
//                Toast.makeText(this, "Storage Read Write Permission Denied!", Toast.LENGTH_SHORT).show()
                showPermissionDeniedDialog()
                Log.i(TAG,"Storage Read Write and Record Audio Permission Denied!")
            } else {
                Log.i(TAG,"Storage Read Write and Record Audio Permission Accepted!")
                val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fade_out_color)

                if (radioPlayerService!!.isPlaying() && !isRecording) {
                    radioPlayerService?.startRecording()
                    ivRecordingStatus.startAnimation(anim)
                    fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_stop))
                    clRecordingStatus.visibility = View.VISIBLE
                    isRecording = true
                    startAnimation = true
                } else if (isRecording) {
                    radioPlayerService?.stopRecording()
                    ivRecordingStatus.clearAnimation()
                    fabRecord.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record))
                    clRecordingStatus.visibility = View.GONE
                    isRecording = false
                    startAnimation = false
                }
            }
        }

    }

    private val serviceConnection = object : ServiceConnection {
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioPlayerService.PlayerServiceBinder
            radioPlayerService = binder.service
            pIsBound = true
            radioPlayerService?.playerStatusLiveData?.observe(this@RadioPlayerActivity) {
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
                        if (isPlayingFirstTime) {
                            countDownTimer.start()
                            isPlayingFirstTime = false
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pIsBound = false
            radioPlayerService = null
        }
    }

//    private fun startPlayerService() {
//        RadioPlayerService.newIntent(this, url).also { intent ->
//            startService(intent)
//        }
//    }


    private fun bindToPlayerService() {
        if (radioPlayerService == null) {
            val intent = Intent(this, RadioPlayerService::class.java)
            intent.putExtra(STATION_URL, url)
            intent.putExtra(STATION_NAME, stationName)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//            RadioPlayerService.newIntent(this).also { intent ->
//                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//            }
            startService(intent)
        }
    }

    private fun unbindPlayerService() {
        if (radioPlayerService != null) {
            unbindService(serviceConnection)

            radioPlayerService = null
        }
    }

    private fun stopPlayerService() {
//        radioPlayerService?.pause()
        radioPlayerService?.stopRecording()
        unbindPlayerService()
        stopService(Intent(this, RadioPlayerService::class.java))
        radioPlayerService = null
        tvPlayTime.text = "Stopped."
    }

    override fun onStart() {
        super.onStart()
        var m = 0
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
            "EBC National Radio" -> {
                ivPlayer.setImageResource(R.drawable.ebc_national_radio_01)
            } else -> {
                ivPlayer.setImageResource(R.drawable.ic_baseline_radio)
            }
        }
    }

    override fun onDestroy() {
        stopPlayerService()
        super.onDestroy()
    }

    override fun onBackPressed() {
        stopPlayerService()
        super.onBackPressed()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
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
//                Log.d("RadioPlayerActivity", "ID: ${mp!!.audioSessionId}")

//                initialiseSeekBar()
            }
            mp?.start()
//            Log.d("RadioPlayerActivity", "Duration: ${mp!!.duration/1000} seconds")
        }

        fabPause.setOnClickListener {
            if (mp !== null) mp?.pause()
//            Log.d("RadioPlayerActivity", "Paused at: ${mp!!.currentPosition/1000} seconds")
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