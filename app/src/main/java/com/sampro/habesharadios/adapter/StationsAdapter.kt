package com.sampro.habesharadios.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sampro.habesharadios.R
import com.sampro.habesharadios.model.StationModel

class StationsAdapter(val context: Context, private var stationsList: MutableList<StationModel>) : RecyclerView.Adapter<StationsAdapter.ViewHolder>() {

    private lateinit var mListener : OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.radio_list_layout,
                parent,
                false
            ), mListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = stationsList[position]
        holder.tvStationName.text = station.name
        when (station.name) {
            "Afro FM 105.3" -> {
                holder.ivStationLogo.setImageResource(R.drawable.afro_fm_01)
            }
            "Sheger FM 102.1" -> {
                holder.ivStationLogo.setImageResource(R.drawable.sheger_fm_01)
            }
            "Ahadu FM 94.3" -> {
                holder.ivStationLogo.setImageResource(R.drawable.ahadu_fm_01)
            }
            "Awash FM 90.7" -> {
                holder.ivStationLogo.setImageResource(R.drawable.awash_fm_01)
            }
            "Bisrat FM 101.1" -> {
                holder.ivStationLogo.setImageResource(R.drawable.bisrat_fm_01)
            }
            "EBC FM Addis 97.1" -> {
                holder.ivStationLogo.setImageResource(R.drawable.ebc_fm_addis_01)
            }
            "Ethio FM 107.8" -> {
                holder.ivStationLogo.setImageResource(R.drawable.ethio_fm_01)
            }
        }
    }

    override fun getItemCount(): Int {
        return stationsList.size
    }

    class ViewHolder(view: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        val ivStationLogo: ImageView = view.findViewById(R.id.ivStationLogo)
        val tvStationName: TextView = view.findViewById(R.id.tvStationName)
        init {
            view.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)
            }
        }
    }
}