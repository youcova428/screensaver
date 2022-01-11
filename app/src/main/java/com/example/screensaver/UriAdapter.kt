package com.example.screensaver

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView

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
        var uri = dataSet[position]
//        if (SDK_INT >= Build.VERSION_CODES.P) {
//            val contentResolver = context.contentResolver
////            val takeFlags: Int =
////                Intent(Intent.ACTION_OPEN_DOCUMENT).flags and Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            //fixme SecurityException
//            contentResolver.takePersistableUriPermission(uri, takeFlags)
//        }
        holder.imageView!!.setImageURI(uri)
        holder.imageView!!.setOnClickListener {
            listener.OnItemClick(dataSet[position])
        }
        holder.imageView!!.setOnLongClickListener {
            longClickListener.OnItemLongClick(position)
            return@setOnLongClickListener true
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
        notifyDataSetChanged()
        return this.dataSet
    }

}