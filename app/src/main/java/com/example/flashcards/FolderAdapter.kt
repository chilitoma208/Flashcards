package com.example.flashcards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface FolderMenuListener {
    fun menuTapped(fid: String)
    fun folderTapped(fid: String, fname: String)
}

class FolderAdapter(private val FolderList :ArrayList<MyFolder>, private val listener: FolderMenuListener) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name: TextView = view.findViewById(R.id.folder_title)
        val button: ImageButton = view.findViewById(R.id.folder_menu)
        val image: ImageView = view.findViewById(R.id.upfolder)

    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.folder_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val folder = FolderList[position]
        viewHolder.name.text = folder.folder_name

        viewHolder.button.setOnClickListener {
            listener.menuTapped(folder.folder_num.toString())
        }
        viewHolder.name.setOnClickListener{
            listener.folderTapped(folder.folder_num.toString(), folder.folder_name)
        }

    }

    // 表示数を返す
    override fun getItemCount() = FolderList.size
}

