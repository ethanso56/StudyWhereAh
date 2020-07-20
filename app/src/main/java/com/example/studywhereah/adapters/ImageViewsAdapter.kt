package com.example.studywhereah.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.studywhereah.R
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.map_activity_image.view.*

class ImageViewsAdapter(data: ArrayList<StorageReference>)
    : RecyclerView.Adapter<ImageViewsAdapter.ViewHolder>() {

    var inputArrList = data

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imgView = view.findViewById<ImageView>(R.id.iv_map_activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewsAdapter.ViewHolder {
        var itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.map_activity_image, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return inputArrList.size
    }

    override fun onBindViewHolder(holder: ImageViewsAdapter.ViewHolder, position: Int) {
        var task = inputArrList.get(position).getBytes(350 * 1000)
        task.addOnSuccessListener { byteArr ->
            var bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            holder.imgView.setImageBitmap(bitmap)
        }
    }


}