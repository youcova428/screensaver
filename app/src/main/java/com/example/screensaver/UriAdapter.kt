package com.example.screensaver

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class UriAdapter(private val dataSet: MutableList<Uri>,private val context: Context) :
    RecyclerView.Adapter<UriAdapter.ViewHolder>() {

    private lateinit var listener: OnImageItemClickListener
    private lateinit var longClickListener: OnImageItemLongClickListener

    /**
     * アイテムクリックリスナーのインターフェース
     */
    interface OnImageItemClickListener {
        fun OnItemClick(uri: Uri)
    }

    /**
     * アイテムロングクリックリスナーのインターフェース
     */
    interface OnImageItemLongClickListener {
        fun OnItemLongClick(position: Int)
    }

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
        holder.imageView?.let {
            it.load(dataSet[position])
            it.setOnClickListener {
                listener.OnItemClick(dataSet[position])
            }
            it.setOnLongClickListener {
                longClickListener.OnItemLongClick(position)
                return@setOnLongClickListener true
            }
        }
    }

    /**
     * データ数を数えるメソッド
     */
    override fun getItemCount() = dataSet.size

    /**
     * アダプターの各アイテムのクリックリスナー
     * @param listener
     */
    fun setOnImageItemClickListener(listener: OnImageItemClickListener) {
        this.listener = listener
    }

    /**
     * アダプターの各アイテムのクリックリスナー
     * @param longClickListener
     */
    fun setImageItemLongClickListener(longClickListener: OnImageItemLongClickListener) {
        this.longClickListener = longClickListener
    }

    /**
     * アイテムを追加する
     */
    fun updateItem(newUris: Array<Uri>) {
        this.dataSet.clear()
        this.dataSet.addAll(newUris)
    }

    /**
     * アイテムを削除する
     */
    fun removeItem(position: Int): MutableList<Uri> {
        this.dataSet.removeAt(position)
        return this.dataSet
    }

}