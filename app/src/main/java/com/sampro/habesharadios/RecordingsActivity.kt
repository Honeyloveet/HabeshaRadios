package com.sampro.habesharadios

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sampro.habesharadios.adapter.RecordingsAdapter
import java.io.File
import java.util.ArrayList

class RecordingsActivity : AppCompatActivity() {

    private lateinit var rvRecordings: RecyclerView
    private lateinit var adapter: RecordingsAdapter
    private var recordings: MutableList<Pair<File, Int>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        rvRecordings = findViewById(R.id.rvRecordings)
        val layoutManager = LinearLayoutManager(this)
        rvRecordings.layoutManager = layoutManager
        createRecordingsList()
        adapter = RecordingsAdapter(this, recordings)
        rvRecordings.adapter = adapter

        adapter.setOnItemClickListener(object : RecordingsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                Log.d("Files", "Position $position Clicked!!")
            }

        })

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
                recordings.add(Pair(file[i], i))
            }
        } else {
            Log.d("Files", "There are no radio recordings.")
        }
    }
}