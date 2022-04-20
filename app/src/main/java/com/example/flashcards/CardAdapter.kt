package com.example.flashcards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


interface CardMenuListener {
    fun menuTapped(cid: String)
    fun cardTapped(cid: String, cname: String)
    fun imageTapped(cid: String, cname: String)
}

class CardAdapter(private val CardList: ArrayList<MyCard>, private val listener: CardMenuListener) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name: TextView = view.findViewById(R.id.card_title)
        val button: ImageButton = view.findViewById(R.id.card_menu)
        val image: ImageView = view.findViewById(R.id.card_image)

    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_item, viewGroup, false)
        return ViewHolder(view)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val card = CardList[position]
        viewHolder.name.text = card.card_name

        viewHolder.button.setOnClickListener {
            listener.menuTapped(card.card_num.toString())
        }
        viewHolder.name.setOnClickListener{
            listener.cardTapped(card.card_num.toString(), card.card_name)
        }
        viewHolder.image.setOnClickListener{
            listener.imageTapped(card.card_num.toString(), card.card_name)
        }

    }

    // 表示数を返す
    override fun getItemCount() = CardList.size
}