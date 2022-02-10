package com.sampro.habesharadios.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sampro.habesharadios.R
import java.io.File

class RecordingsAdapter(val context: Context, private val recordingsList: List<Pair<File, Int>>) : RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {

    private lateinit var mListener : OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onImgDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.recordings_list_layout,
                parent,
                false
            ), mListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recordingsList[position]
        holder.tvNumber.text = (position + 1).toString()
        holder.tvRecording.text = item.first.name

//        holder.imageBtnDelete.setOnClickListener {
////            Toast.makeText(context, "${position + 1} ${item.first.name}", Toast.LENGTH_SHORT).show()
//            Log.d("Files", "${item.first.name} Delete Button Clicked!!.")
//        }
    }

    override fun getItemCount(): Int {
        return recordingsList.size
    }

    class ViewHolder(view: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvNumber)
        val tvRecording: TextView = view.findViewById(R.id.tvRecording)
        private val imageBtnDelete: ImageButton = view.findViewById(R.id.imageBtnDelete)

        init {
            view.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)
            }
            imageBtnDelete.setOnClickListener {
                listener.onImgDeleteClick(absoluteAdapterPosition)
            }
        }
    }

}