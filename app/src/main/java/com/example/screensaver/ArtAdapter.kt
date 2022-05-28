package com.example.screensaver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ArtAdapter(var artList: MutableList<Art>) : RecyclerView.Adapter<ArtAdapter.ViewHolder>() {

    private lateinit var listener: OnArtItemClickListener

    interface OnArtItemClickListener {
        fun onArtItemClick(art: Art, view: View, artList: MutableList<Art>)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView? = null
        init {
            imageView = view.findViewById(R.id.recyclerview_art_item_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_art_item, parent, false)
        return ArtAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtAdapter.ViewHolder, position: Int) {
        holder.imageView?.let {
            it.load(artList[position].primaryImage)
            it.setOnClickListener {
                listener.onArtItemClick(artList[position], it, artList)
            }
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }

    fun setOnArtItemClickListener(listener: OnArtItemClickListener) {
        this.listener = listener
    }

}