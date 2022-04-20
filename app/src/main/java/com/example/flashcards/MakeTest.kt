package com.example.flashcards

import android.annotation.SuppressLint
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import kotlin.collections.ArrayList as Array

class MakeTest : AppCompatActivity() {
    private var arrayListFlag: Array<Boolean> = arrayListOf()
    private var arrayListId: Array<Int> = arrayListOf()
    private var arrayListTag: Array<Int> = arrayListOf()
    private var arrayListQuestion: Array<String> = arrayListOf()
    private var arrayListAnswer: Array<String> = arrayListOf()
    private var arrayListExplain: Array<String> = arrayListOf()
    private var arraySelectId: Array<Int> = arrayListOf()
    private var arraySelectTag: Array<Int> = arrayListOf()
    private var arraySelectQuestion: Array<String> = arrayListOf()
    private var arraySelectAnswer: Array<String> = arrayListOf()
    private var list: Array<TestData> = arrayListOf()

    private val _helper = DatabaseHelper(this)
    private var cardId: String = ""
    private var cardName: String = ""
    private var folderId: String = ""
    private var flag: Boolean = false
    private var moodAll: Boolean = false
    private var random: Boolean = false
    private var counter = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_test)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        moodAll = intent.getBooleanExtra("moodAll",false)
        if (moodAll){
            cardId = intent.getStringExtra("folderId").toString()
            cardName = intent.getStringExtra("folderName").toString()
            folderId = intent.getStringExtra("folderId").toString()
            flag = intent.getBooleanExtra("flag", false)
            random = intent.getBooleanExtra("random", false)
        }else{
            cardId = intent.getStringExtra("cardId").toString()
            cardName = intent.getStringExtra("cardName").toString()
            folderId = intent.getStringExtra("folderId").toString()
            flag = intent.getBooleanExtra("flag", false)
            random = intent.getBooleanExtra("random", false)
        }
        title = cardName

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
                arrayListExplain.add(cursor.getString(5).replace("\\n", "\n"))
                cursor.moveToNext()
            }
        }
        cursor.close()

        val sql2 = "SELECT f_id, tag, question, answer FROM FlashcardTable WHERE id = $folderId"
        val cursor2 = db.rawQuery(sql2, null)
        if (cursor2.count > 0) {
            cursor2.moveToFirst()
            while (!cursor2.isAfterLast) {
                arraySelectId.add(cursor2.getInt(0))
                arraySelectTag.add(cursor2.getInt(1))
                arraySelectQuestion.add(cursor2.getString(2))
                arraySelectAnswer.add(cursor2.getString(3))
                cursor2.moveToNext()
            }
        }
        cursor2.close()



        val count = arrayListId.count() - 1
        val count2 = arraySelectId.count() - 1
        val indexList = (0..count).toMutableList()
        val selectList = (0..count2).toMutableList()
        if (random) indexList.shuffle()
        val selectAnswer = arrayListOf<String>()
        val selectQuestion = arrayListOf<String>()

        if (count != -1) {
            Log.d("aaa", count.toString())
            list.clear()
            for (i in 0..count) {
                val tag: Int = arrayListTag[indexList[i]]
                selectAnswer.clear()
                selectQuestion.clear()
                selectAnswer.add(arrayListAnswer[indexList[i]])
                selectQuestion.add(arrayListQuestion[indexList[i]])
                selectList.shuffle()
                var j = 0
                while (selectAnswer.count() < 4) {
                    if (tag == arraySelectTag[selectList[j]] && arraySelectId[selectList[j]] != arrayListId[indexList[i]]) {
                        selectAnswer.add(arraySelectAnswer[selectList[j]])
                        selectQuestion.add(arraySelectQuestion[selectList[j]])
                    }
                    j += 1
                }
                val order = (0..3).toMutableList()
                order.shuffle()
                list.add(
                    i,
                    TestData(
                        arrayListId[indexList[i]],
                        arrayListFlag[indexList[i]],
                        arrayListQuestion[indexList[i]],
                        arrayListAnswer[indexList[i]],
                        arrayListExplain[indexList[i]],
                        selectAnswer[order[0]],
                        selectAnswer[order[1]],
                        selectAnswer[order[2]],
                        selectAnswer[order[3]],
                        selectQuestion[order[0]],
                        selectQuestion[order[1]],
                        selectQuestion[order[2]],
                        selectQuestion[order[3]]
                    )
                )
            }
            val correctImage: ImageView = findViewById(R.id.correct)
            val cText: TextView = findViewById(R.id.counter)
            val check: CheckBox = findViewById(R.id.check2)
            val question: TextView = findViewById(R.id.question2)
            val choiceA: TextView = findViewById(R.id.choiceA)
            val choiceB: TextView = findViewById(R.id.choiceB)
            val choiceC: TextView = findViewById(R.id.choiceC)
            val choiceD: TextView = findViewById(R.id.choiceD)

            cText.text = "${counter+1}/ ${arrayListId.count()}"
            check.isChecked = list[counter].check
            question.text = list[counter].question
            choiceA.text = list[counter].selectionA
            choiceB.text = list[counter].selectionB
            choiceC.text = list[counter].selectionC
            choiceD.text = list[counter].selectionD

            check.setOnClickListener {
                list[counter].check = check.isChecked
                if(check.isChecked) {
                    changeFlag(list[counter].id.toString(), 1)
                }else {
                    changeFlag(list[counter].id.toString(), 0)
                }
            }

            val buttonP: ImageButton = findViewById(R.id.previous)
            val buttonN: ImageButton = findViewById(R.id.next)
            buttonP.isEnabled = false
            var isOk: String = list[counter].answer

            buttonP.setOnClickListener {
                correctImage.visibility = View.INVISIBLE
                counter -= 1
                check.isChecked = list[counter].check
                question.text = list[counter].question
                choiceA.text = list[counter].selectionA
                choiceB.text = list[counter].selectionB
                choiceC.text = list[counter].selectionC
                choiceD.text = list[counter].selectionD
                cText.text = "${counter+1}/ ${arrayListId.count()}"
                isOk = list[counter].answer
                buttonP.isEnabled = counter != 0
                buttonN.isEnabled = counter+1 != arrayListId.count()
            }
            buttonN.setOnClickListener {
                correctImage.visibility = View.INVISIBLE
                counter += 1
                check.isChecked = list[counter].check
                question.text = list[counter].question
                choiceA.text = list[counter].selectionA
                choiceB.text = list[counter].selectionB
                choiceC.text = list[counter].selectionC
                choiceD.text = list[counter].selectionD
                cText.text = "${counter+1}/ ${arrayListId.count()}"
                isOk = list[counter].answer
                buttonP.isEnabled = counter != 0
                buttonN.isEnabled = counter+1 != arrayListId.count()
            }

            val cardA: CardView = findViewById(R.id.A)
            val cardB: CardView = findViewById(R.id.B)
            val cardC: CardView = findViewById(R.id.C)
            val cardD: CardView = findViewById(R.id.D)
            cardA.setOnClickListener {
                if (choiceA.text == isOk) {
                    correctImage.visibility = View.VISIBLE
                    Toast.makeText(this, "correct (^Д^) ", Toast.LENGTH_SHORT).show()
                }else {
                    correctImage.visibility = View.INVISIBLE
                    Toast.makeText(this, "incorrect (;_;)", Toast.LENGTH_SHORT).show()
                }
            }
            cardB.setOnClickListener {
                if (choiceB.text == isOk) {
                    correctImage.visibility = View.VISIBLE
                    Toast.makeText(this, "correct (^Д^) ", Toast.LENGTH_SHORT).show()
                }else {
                    correctImage.visibility = View.INVISIBLE
                    Toast.makeText(this, "incorrect (;_;)", Toast.LENGTH_SHORT).show()
                }
            }
            cardC.setOnClickListener {
                if (choiceC.text == isOk) {
                    correctImage.visibility = View.VISIBLE
                    Toast.makeText(this, "correct (^Д^) ", Toast.LENGTH_SHORT).show()
                }else {
                    correctImage.visibility = View.INVISIBLE
                    Toast.makeText(this, "incorrect (;_;)", Toast.LENGTH_SHORT).show()
                }
            }
            cardD.setOnClickListener {
                if (choiceD.text == isOk) {
                    correctImage.visibility = View.VISIBLE
                    Toast.makeText(this, "correct (^Д^) ", Toast.LENGTH_SHORT).show()
                }else {
                    correctImage.visibility = View.INVISIBLE
                    Toast.makeText(this, "incorrect (;_;)", Toast.LENGTH_SHORT).show()
                }
            }

            //解説を表示する
            val answer: ImageButton = findViewById(R.id.answer)
            answer.setOnClickListener {
                val builder = AlertDialog.Builder(this, R.style.explain)
                val inflater = this.layoutInflater
                val aView = inflater.inflate(R.layout.answer_dialog, null)
                val check3: CheckBox = aView.findViewById(R.id.check3)
                val answer2: TextView = aView.findViewById(R.id.answer2)
                val explain2: TextView = aView.findViewById(R.id.explain2)
                val choiceA2: TextView = aView.findViewById(R.id.choiceA2)
                val answerA: TextView = aView.findViewById(R.id.answerA)
                val choiceB2: TextView = aView.findViewById(R.id.choiceB2)
                val answerB: TextView = aView.findViewById(R.id.answerB)
                val choiceC2: TextView = aView.findViewById(R.id.choiceC2)
                val answerC: TextView = aView.findViewById(R.id.answerC)
                val choiceD2: TextView = aView.findViewById(R.id.choiceD2)
                val answerD: TextView = aView.findViewById(R.id.answerD)
                val close: ImageButton = aView.findViewById(R.id.close)

                check3.isChecked = list[counter].check
                answer2.text = isOk
                explain2.text = list[counter].explain
                choiceA2.text = list[counter].selectionA
                answerA.text = list[counter].answerA
                choiceB2.text = list[counter].selectionB
                answerB.text = list[counter].answerB
                choiceC2.text = list[counter].selectionC
                answerC.text = list[counter].answerC
                choiceD2.text = list[counter].selectionD
                answerD.text = list[counter].answerD

                builder.setView(aView)
                val dialog = builder.create()
                dialog.show()

                close.setOnClickListener {
                    dialog.dismiss()
                }
            }
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        _helper.close()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
