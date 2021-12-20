package com.example.screensaver

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class UriAdapter(private val dataSet: Array<Uri>) : RecyclerView.Adapter<UriAdapter.ViewHolder>() {

    /**
     * リスト内の各アイテムのレイアウト内を含むViewのラッパー
     * @param view
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView? = null

        init {
            imageView = view.findViewById(R.id.recyclerview_item_image)
        }

    }

    /**
     * ViewHolderとRecyclerViewに関連するViewを形成するメソッド
     * @param parent　ViewGroup
     * @param viewType Viewの型
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_image_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * ViewHolderをデータと関連付けるメソッド
     * @param holder
     * @param position 配列の位置
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView!!.setImageURI(dataSet[position])
    }

    /**
     * データ数を数えるメソッド
     */
    override fun getItemCount() = dataSet.size

    //todo ItemClickListener interface? 実装
}