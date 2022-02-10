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
import com.sampro.habesharadios.adapter.RecordingsAdapter
import com.sampro.habesharadios.model.StationsModelParcelable
import java.io.File
import java.util.ArrayList

class RecordingsActivity : AppCompatActivity() {

    private lateinit var rvRecordings: RecyclerView
    private lateinit var adapter: RecordingsAdapter
    private var recordings: MutableList<Pair<File, Int>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        supportActionBar?.apply {
            title = "Radio Recordings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

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
                val x = fileToDelete.toUri()
                showDeleteConfirmationDialog(fileToDelete)
            }

        })

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