package com.sampro.habesharadios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sampro.habesharadios.adapter.StationsAdapter
import com.sampro.habesharadios.model.StationModel
import com.sampro.habesharadios.utils.RadioStations
import java.util.*
import kotlin.time.Duration.Companion.minutes

class MainActivity : AppCompatActivity() {

    private companion object{
        private const val TAG = "SAMUEL"
    }

    private lateinit var adViewMain: AdView
    private var mInterstitialAd: InterstitialAd? = null

    private lateinit var rvStations: RecyclerView
    private lateinit var adapter: StationsAdapter
    private lateinit var stations: MutableList<StationModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setDarkTheme()



        supportActionBar?.apply {
            title = "Radio Recordings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        MobileAds.initialize(this)

        loadInterstitialAd()

        adViewMain = findViewById(R.id.adViewMain)
        rvStations = findViewById(R.id.rvStations)
        val layoutManager = LinearLayoutManager(this)
        rvStations.layoutManager = layoutManager
        stations = mutableListOf()
        createStationsList()
        adapter = StationsAdapter(this, stations)
        rvStations.adapter = adapter
//        adapter.notifyDataSetChanged()
        val adRequest = AdRequest.Builder().build()
        adViewMain.loadAd(adRequest)

        adViewMain.adListener = object : AdListener() {

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }

            override fun onAdOpened() {
                super.onAdOpened()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
            }

            override fun onAdClicked() {
                super.onAdClicked()
            }

            override fun onAdImpression() {
                super.onAdImpression()
            }
        }

//        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712",AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
//            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                Log.d(TAG, loadAdError.message)
//                mInterstitialAd = null
//            }
//
//            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                Log.d(TAG, "Ad was Loaded.")
//                mInterstitialAd = interstitialAd
//            }
//        })


        adapter.setOnItemClickListener(object : StationsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val randomNumber = (1..50).random()
                val y = Date().time
                Log.d(TAG,"$randomNumber")

                val isNumEven = checkEvenOrOddNumber(randomNumber)
                if (isNumEven) {
                    showInterstitialAd(position)
                } else {
                    val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
                    intent.putExtra("name", stations[position].name)
                    intent.putExtra("url", stations[position].url)
                    startActivity(intent)
                }
                //showInterstitialAd(position)
//                val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
//                intent.putExtra("name", stations[position].name)
//                intent.putExtra("url", stations[position].url)
//
//                startActivity(intent)

            }

        })
//        cvAhaduFM.setOnClickListener {
//            val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
//            intent.putExtra("station","Ahadu FM")
//            startActivity(intent)
//        }

    }

    private fun checkEvenOrOddNumber(number: Int) : Boolean {
        return number % 2 == 0  // returns true if number is even or false if number is odd
    }

    private fun showInterstitialAd(position: Int) {

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        mInterstitialAd = null
                        Log.d(TAG,"InterstitialAd Closed")

                        val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
                        intent.putExtra("name", stations[position].name)
                        intent.putExtra("url", stations[position].url)

                        startActivity(intent)
                        // Toast.makeText(this@MainActivity,"Ad Closed!", Toast.LENGTH_SHORT).show()
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
            mInterstitialAd?.show(this)

        } else {

            Log.d(TAG, "The interstitial ad wasn't ready yet.")

            val intent = Intent(this@MainActivity, RadioPlayerActivity::class.java)
            intent.putExtra("name", stations[position].name)
            intent.putExtra("url", stations[position].url)

            startActivity(intent)
        }

    }

    private fun loadInterstitialAd() {

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712",adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d(TAG, loadAdError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "InterstitialAd was Loaded.")
                mInterstitialAd = interstitialAd
            }
        })

    }

    override fun onResume() {
        super.onResume()
        loadInterstitialAd()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemRecordings -> {
                val intent = Intent(this, RecordingsActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setDarkTheme() {
        val theme = AppCompatDelegate.MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    private fun createStationsList() {
        for (station in RadioStations.stations) {
            val singleStation = StationModel().apply {
                name = station.key
                url = station.value
            }
            stations.add(singleStation)
        }
        if (stations.isNotEmpty()) {
            stations.sortBy {
                it.name
            }
        }
    }
}