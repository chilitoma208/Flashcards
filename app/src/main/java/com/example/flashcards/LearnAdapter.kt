package com.example.flashcards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.learn_card.view.*
import net.cachapa.expandablelayout.ExpandableLayout

interface LearnMenuListener {
    fun menuTapped(fid: String, tag: String, q: String, a: String, e: String)
    fun checkTapped(fid: String, checked: Boolean)
}

class LearnAdapter(private val LearnList :ArrayList<MyLearn>, private val listener: LearnMenuListener) : RecyclerView.Adapter<LearnAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var currentItem: MyLearn? = null
        var check: CheckBox = view.findViewById(R.id.check)
        internal val question: TextView = view.findViewById(R.id.answer1)
        private val explain: TextView = view.findViewById(R.id.explain1)
        val button: ImageButton = view.findViewById(R.id.flash_menu)
        private val arrow: ImageView = view.findViewById(R.id.expand_arrow)
        private val expandedLayout: ExpandableLayout = view.findViewById(R.id.expandable_layout)
        private val titleLayout: LinearLayout = view.findViewById(R.id.title_layout)

        init {
            view.expand_arrow.setOnClickListener{
                currentItem?.let{
                    val expanded = it.isExpanded
                    it.isExpanded = expanded.not()
                    titleLayout.isSelected = expanded.not()

                    expandedLayout.toggle()

                    val anim = RotateAnimation(
                        0f,
                        180f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    ).apply {
                        duration = 300
                        fillAfter = true
                    }
                    arrow.startAnimation(anim)
                }
            }
        }
        fun bind(item: MyLearn) {
            currentItem = item

            check.isChecked = item.check
            question.text = item.flash_q
            //item.flash_a
            explain.text = item.flash_e.replace("\\n", "\n")

            if (item.isExpanded) {
                expandedLayout.expand(false)
            } else {
                expandedLayout.collapse(false)
            }
            titleLayout.isSelected = item.isExpanded.not()
        }
    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.learn_card, viewGroup, false)
        return ViewHolder(view)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val learn = LearnList[position]
        var count = 0
        viewHolder.bind(learn)

        viewHolder.button.setOnClickListener {
            listener.menuTapped(learn.flash_id.toString(), learn.tag.toString(), learn.flash_q, learn.flash_a, learn.flash_e)
        }
        viewHolder.check.setOnClickListener{
            val checked: Boolean = viewHolder.check.isChecked
            listener.checkTapped(learn.flash_id.toString(),checked)
        }
        viewHolder.question.setOnClickListener {
            if ((count % 2) == 0) {
                viewHolder.question.text = learn.flash_a
            }else viewHolder.question.text = learn.flash_q
            count += 1
        }
    }

    // 表示数を返す
    override fun getItemCount() = LearnList.size
}