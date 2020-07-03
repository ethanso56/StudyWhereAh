package com.example.studywhereah.activities

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studywhereah.R
import kotlinx.android.synthetic.main.item_saved_locations_row.view.*

class SavedLocationsAdaptor(val context: Context, val names: ArrayList<String>, val address: ArrayList<String>) :
    RecyclerView.Adapter<SavedLocationsAdaptor.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val llSavedLocationsMainItem = view.ll_saved_locations_item_main
        val tvName = view.tvName
        val tvAddress = view.tvAddress
        val ivDelete = view.ivDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
            R.layout.item_saved_locations_row, parent, false
        ))
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name : String = names[position]
        val address : String = address[position]

        holder.tvName.text = name
        holder.tvAddress.text = address

        if (position % 2 == 0) {
            holder.llSavedLocationsMainItem.setBackgroundColor(
                Color.parseColor("#EBEBEB")
            )
        } else {
            holder.llSavedLocationsMainItem.setBackgroundColor(
                Color.parseColor("#FFFFFF")
            )
        }

        holder.ivDelete.setOnClickListener{ view ->
            if (context is SavedLocationsActivity) {
                context.deleteSavedLocationAlertDialog(name)
            }
        }
    }
}