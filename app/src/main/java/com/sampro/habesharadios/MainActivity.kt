package com.sampro.habesharadios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sampro.habesharadios.adapter.StationsAdapter
import com.sampro.habesharadios.model.StationModel
import com.sampro.habesharadios.utils.RadioStations

class MainActivity : AppCompatActivity() {

    private companion object{
        private const val TAG = "SAMUEL"
    }

    private lateinit var rvStations: RecyclerView
    private lateinit var adapter: StationsAdapter
    private lateinit var stations: MutableList<StationModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setDarkTheme()

        rvStations = findViewById(R.id.rvStations)
        val layoutManager = LinearLayoutManager(this)
        rvStations.layoutManager = layoutManager
        stations = mutableListOf()
        createStationsList()
        adapter = StationsAdapter(this, stations)
        rvStations.adapter = adapter
//        adapter.notifyDataSetChanged()

        adapter.setOnItemClickListener(object : StationsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                intent.putExtra("name", stations[position].name)
                intent.putExtra("url", stations[position].url)

                startActivity(intent)
            }

        })
//        cvAhaduFM.setOnClickListener {
//            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
//            intent.putExtra("station","Ahadu FM")
//            startActivity(intent)
//        }

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