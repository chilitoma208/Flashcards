package com.example.flashcards

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Learning : AppCompatActivity(), LearnMenuListener{
    private var arrayListFlag: ArrayList<Boolean> = arrayListOf()
    private var arrayListId: ArrayList<Int> = arrayListOf()
    private var arrayListTag: ArrayList<Int> = arrayListOf()
    private var arrayListQuestion: ArrayList<String> = arrayListOf()
    private var arrayListAnswer: ArrayList<String> = arrayListOf()
    private var arrayListExplain: ArrayList<String> = arrayListOf()

    private val _helper = DatabaseHelper(this)
    private var cardId: String = ""
    private var cardName: String = ""
    private var flag: Boolean = false
    private var moodAll: Boolean = false
    private lateinit var myAdapter: LearnAdapter
    private var list: ArrayList<MyLearn> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        moodAll = intent.getBooleanExtra("moodAll", false)
        if (moodAll){
            cardId = intent.getStringExtra("folderId").toString()
            cardName = intent.getStringExtra("folderName").toString()
            flag = intent.getBooleanExtra("flag", false)
        }else{
            cardId = intent.getStringExtra("cardId").toString()
            cardName = intent.getStringExtra("cardName").toString()
            flag = intent.getBooleanExtra("flag", false)
        }
        title = cardName

        onShow()

    }

    override fun onResume() {
        super.onResume()
        onShow()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        _helper.close()
        return super.onSupportNavigateUp()
    }

    private fun onShow() {
        try {
            arrayListFlag.clear()
            arrayListId.clear()
            arrayListTag.clear()
            arrayListQuestion.clear()
            arrayListAnswer.clear()
            arrayListExplain.clear()

            val db = _helper.readableDatabase
            // データベースを検索
            val sql = if (moodAll && flag) {
                "SELECT f_id, flag, tag, question, answer, explanation FROM FlashcardTable WHERE id = $cardId AND flag = 1"
            }else {
                if (moodAll && !flag) {
                    "SELECT f_id, flag, tag, question, answer, explanation FROM FlashcardTable WHERE id = $cardId"
                } else {
                    if (!moodAll && flag) {
                        "SELECT f_id, flag, tag, question, answer, explanation FROM FlashcardTable WHERE card_id = $cardId AND flag = 1"
                    } else
                        "SELECT f_id, flag, tag, question, answer, explanation FROM FlashcardTable WHERE card_id = $cardId"
                }
            }
            val cursor = db.rawQuery(sql, null)

            // 検索結果から取得する項目を定義
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    arrayListId.add(cursor.getInt(0))
                    if (cursor.getInt(1) == 0){
                        arrayListFlag.add(false)
                    }else arrayListFlag.add(true)
                    arrayListTag.add(cursor.getInt(2))
                    arrayListQuestion.add(cursor.getString(3))
                    arrayListAnswer.add(cursor.getString(4))
                    arrayListExplain.add(cursor.getString(5))
                    cursor.moveToNext()
                }
            }
            cursor.close()

            // ListViewにアダプターを設定
            list.clear()
            for (i in 1 .. arrayListId.count()) {
                list.add(i - 1, MyLearn(
                    arrayListId[i - 1],
                    arrayListFlag[i - 1],
                    arrayListTag[i - 1],
                    arrayListQuestion[i - 1],
                    arrayListAnswer[i - 1],
                    arrayListExplain[i - 1]
                ))
            }

            val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)
            myAdapter = LearnAdapter(list, this)
            recyclerView.adapter = myAdapter

        }catch(exception: Exception) {
            Log.e("selectData", exception.toString())
        }
    }
    override fun menuTapped(fid: String,t: String, q: String, a: String, e: String){
        val item = arrayOf("編集", "削除")
        AlertDialog.Builder(this)
            .setItems(item) { _, which ->
                if (which == 0) {
                    val dialog2 = AlertDialog.Builder(this)
                    val inflater = this.layoutInflater
                    val fView = inflater.inflate(R.layout.dialog_add_flash, null)
                    val tag2: EditText = fView.findViewById(R.id.tag)
                    val q2: EditText = fView.findViewById(R.id.question)
                    val a2: EditText = fView.findViewById(R.id.answer)
                    val e2: EditText = fView.findViewById(R.id.explanation)
                    tag2.setText(t)
                    q2.setText(q)
                    a2.setText(a)
                    e2.setText(e)
                    dialog2.setView(fView)
                    dialog2.setPositiveButton("save") {dialog, _ ->
                        val tag = fView?.findViewById<EditText>(R.id.tag)?.text
                        val question = fView?.findViewById<EditText>(R.id.question)?.text
                        val answer = fView?.findViewById<EditText>(R.id.answer)?.text
                        val explanation = fView?.findViewById<EditText>(R.id.explanation)?.text

                        if (!(question.isNullOrEmpty() || answer.isNullOrEmpty() || explanation.isNullOrEmpty())) {
                            if (!tag.isNullOrEmpty()) {
                                updateFlash(fid, tag.toString(), question.toString(), answer.toString(), explanation.toString())
                            }
                            else {
                                updateFlash(fid, null, question.toString(), answer.toString(), explanation.toString())
                            }
                        }
                        else {
                            Toast.makeText(this, "is canceled",Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    dialog2.setNeutralButton("cancel") {dialog, _ ->
                        Toast.makeText(this, "is canceled",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    dialog2.show()
                }
                if (which == 1) {
                    AlertDialog.Builder(this)
                        .setMessage("本当に削除しますか？")
                        .setPositiveButton("はい") { _, _ ->
                            deleteFlash(fid)
                            onShow()
                        }
                        .setNeutralButton("いいえ") { _, _ ->

                        }
                        .show()
                }
            }
            .show()
    }

    override fun checkTapped(fid: String, checked: Boolean) {
        if(checked) {
            changeFlag(fid, 1)
        }else {
            changeFlag(fid, 0)
        }
    }
    //Flag 変更
    private fun changeFlag(fid: String, c: Int) {
        try {
            val db = _helper.writableDatabase
            val values = ContentValues()
            values.put("flag", c)
            val whereClauses = "f_id = ?"
            val whereArgs = arrayOf(fid)
            db.update("FlashcardTable", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }

    //更新
    private fun updateFlash(fid: String, t: String?, q: String, a: String, e: String) {
        try {
            val db = _helper.writableDatabase

            val values = ContentValues()
            values.put("tag", t?.toInt())
            values.put("question", q)
            values.put("answer", a)
            values.put("explanation", e)
            val whereClauses = "f_id = ?"
            val whereArgs = arrayOf(fid)
            db.update("FlashcardTable", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }

    //削除
    private fun deleteFlash(fid: String) {
        try {
            val db = _helper.writableDatabase
            val whereClauses = "f_id = ?"
            val whereArgs = arrayOf(fid)
            db.delete( "FlashcardTable", whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData", exception.toString())
        }
    }


    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}