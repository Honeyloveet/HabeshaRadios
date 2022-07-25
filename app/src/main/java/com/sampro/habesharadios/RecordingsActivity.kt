package com.sampro.habesharadios

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sampro.habesharadios.adapter.RecordingsAdapter
import com.sampro.habesharadios.model.StationsModelParcelable
import java.io.File
import java.util.ArrayList

class RecordingsActivity : AppCompatActivity() {

    private companion object{
        private const val TAG = "SAMUEL"
    }

    private lateinit var rvRecordings: RecyclerView
    private lateinit var adapter: RecordingsAdapter
    private var recordings: MutableList<Pair<File, Int>> = ArrayList()

    private var mInterstitialAd: InterstitialAd? = null

    private var mRewardedAd: RewardedAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        supportActionBar?.apply {
            title = "Radio Recordings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        MobileAds.initialize(this)
//        buildAdd()

        rvRecordings = findViewById(R.id.rvRecordings)
        val layoutManager = LinearLayoutManager(this)
        rvRecordings.layoutManager = layoutManager
        createRecordingsList()
        adapter = RecordingsAdapter(this, recordings)
        rvRecordings.adapter = adapter

        adapter.setOnItemClickListener(object : RecordingsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val stationName = getRadioStationName(recordings[position].first.name)

                val stationToPlay = StationsModelParcelable().apply {
                    name = stationName
                    uri = recordings[position].first.toUri().toString()
                }

                val intent = Intent(this@RecordingsActivity, RecordingsPlayerActivity::class.java)
                intent.putExtra("stationToPlay", stationToPlay)
                Log.d("Files", stationName)
                startActivity(intent)
            }

            override fun onImgDeleteClick(position: Int) {
                val fileToDelete = recordings[position].first
                showDeleteConfirmationDialog(fileToDelete)
            }

        })

//        buildInterstitialAd()
//        buildRewardedAd()
    }

    private fun buildRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Rewarded Ad was Loaded.")
                mRewardedAd = rewardedAd
                mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        mRewardedAd = null
                        /// perform your action here when ad will not load
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        mRewardedAd = null
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        mRewardedAd = null
                        Log.d(TAG,"Add Closed")
                        Toast.makeText(this@RecordingsActivity,"Ad Closed!", Toast.LENGTH_SHORT).show()
                        //// perform your code that you wants to do after ad dismissed or closed
                    }
                }
                loadRewardedAd()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, loadAdError.message)
                mRewardedAd = null
            }

        })
    }

    private fun loadRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd?.show(this) {
                val rewardAmount = it.amount
                val rewardType = it.type
                Log.d(TAG, "User earned the reward $rewardType $rewardAmount.")
            }
        } else {
            Log.d(TAG, "Rewarded Ad wasn't ready yet.")
        }
    }

    private fun buildInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712",adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, loadAdError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was Loaded.")
                mInterstitialAd = interstitialAd
                mInterstitialAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            mInterstitialAd = null
                            Log.d(TAG,"Add Closed")
                            Toast.makeText(this@RecordingsActivity,"Ad Closed!", Toast.LENGTH_SHORT).show()
                            //// perform your code that you wants to do after ad dismissed or closed
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            mInterstitialAd = null

                            /// perform your action here when ad will not load
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            mInterstitialAd = null
                        }
                    }
                loadInterstitialAd()
            }
        })
    }

    private fun loadInterstitialAd() {

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
        }
    }

    private fun getRadioStationName(name: String) : String {
        var stationName = ""
        val startIndex = 0
        val endIndex = name.indexOfAny(arrayListOf("-"), 0)
        for (i in startIndex until endIndex) {
            stationName += name[i]
        }
        return stationName
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirmationDialog(file: File) {

        val dialog = MaterialDialog(this)
            .noAutoDismiss()
            .cancelable(false)
            .cancelOnTouchOutside(false)
            .cornerRadius(14f)
            .customView(R.layout.file_delete_confermation_dialog_layout)

        dialog.findViewById<Button>(R.id.btnDeleteYes).setOnClickListener {
            val isDeleteSuccess = delete(file)
            if (isDeleteSuccess) {
                Toast.makeText(this, "${file.name} Deleted Successfully.", Toast.LENGTH_SHORT).show()
                createRecordingsList()
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "${file.name} Deleted Failed.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnDeleteNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun delete(file: File) : Boolean {
        return file.delete()
    }

    private fun createRecordingsList() {
        val files = File(getExternalFilesDir(null), "/Recordings")
        recordings.clear()

        if (!files.exists()) {
            files.mkdirs()
        }

        if (files.listFiles() != null) {
            val file : Array<File> = files.listFiles()!!

            for (i in file.indices) {
//                val x = file[i].toUri()
                recordings.add(Pair(file[i], i))
            }
        } else {
            Log.d("Files", "There are no radio recordings.")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}