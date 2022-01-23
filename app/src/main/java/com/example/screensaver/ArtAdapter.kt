package com.example.screensaver

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ArtAdapter(var artList: MutableList<Art>) : RecyclerView.Adapter<ArtAdapter.ViewHolder>() {

    private lateinit var listener: OnArtItemClickListener

    interface OnArtItemClickListener {
        fun OnArtItemClick(art: Art)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView? = null
        var titleText : TextView? = null
        init {
            imageView = view.findViewById(R.id.recyclerview_art_item_image)
            titleText = view.findViewById(R.id.recyclerview_art_item_title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_art_item, parent, false)
        return ArtAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtAdapter.ViewHolder, position: Int) {
        Glide.with(holder.imageView!!.context).load(artList[position].primaryImage)
            .into(holder.imageView!!)
        holder.imageView!!.setOnClickListener {
            listener.OnArtItemClick(artList[position])
        }
        holder.titleText!!.text = artList[position].title
    }

    override fun getItemCount(): Int {
        return artList.size
    }

    fun setOnArtItemClickListener(listener: OnArtItemClickListener) {
        this.listener = listener
    }

}