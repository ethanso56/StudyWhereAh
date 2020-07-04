package com.example.studywhereah.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studywhereah.R
import com.example.studywhereah.activities.SavedLocationsActivity
import com.example.studywhereah.models.SavedLocationModel
import kotlinx.android.synthetic.main.item_saved_locations_row.view.*

class SavedLocationsAdaptor(val context: Context, val items: ArrayList<SavedLocationModel>) :
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
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvAddress.text = item.address

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
                context.deleteSavedLocationAlertDialog(item)
            }
        }
    }
}