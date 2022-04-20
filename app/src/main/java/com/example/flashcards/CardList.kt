package com.example.flashcards

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class CardList : AppCompatActivity(), CardAddDialog.DialogListener, CardMenuListener {

    private var arrayListId: ArrayList<Int> = arrayListOf()
    private var arrayListName: ArrayList<String> = arrayListOf()
    private val _helper = DatabaseHelper(this)
    private var folderId: String = ""
    private var folderName: String = ""

    private var mode = true
    private var random = true
    private var mark = false

    private var path = ""
    private lateinit var myAdapter: CardAdapter
    private var list: ArrayList<MyCard> = arrayListOf()


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        folderId = intent.getStringExtra("folderId").toString()
        folderName = intent.getStringExtra("folderName").toString()
        title = folderName


        path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()

        val s1 = findViewById<Switch>(R.id.mood)
        s1.setOnCheckedChangeListener{_ , isChecked ->
            mode = isChecked
        }

        val s2 = findViewById<Switch>(R.id.random)
        s2.setOnCheckedChangeListener { _, isChecked ->
            random = isChecked
        }
        val s3 = findViewById<Switch>(R.id.mark)
        s3.setOnCheckedChangeListener { _, isChecked ->
            mark = isChecked
        }
    }

    override fun onResume() {
        super.onResume()
        onShow()
    }
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(application, MainActivity::class.java)
        startActivity(intent)
        finish()
        _helper.close()
        return super.onSupportNavigateUp()
    }

    private fun onShow() {
        try {
            arrayListId.clear()
            arrayListName.clear()
            val db = _helper.readableDatabase
            // データベースを検索
            val sql = "SELECT card_id, card_name FROM CardNameTable WHERE id = $folderId"
            val cursor = db.rawQuery(sql, null)

            // 検索結果から取得する項目を定義
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    arrayListId.add(cursor.getInt(0))
                    arrayListName.add(cursor.getString(1))
                    cursor.moveToNext()
                }
            }
            cursor.close()

            // ListViewにアダプターを設定
            list.clear()
            for (i in 1 .. arrayListName.count()) {
                list.add(i - 1, MyCard(
                    arrayListId[i - 1],
                    arrayListName[i - 1]
                ))
            }

            val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)
            myAdapter = CardAdapter(list, this)
            recyclerView.adapter = myAdapter

        }catch(exception: Exception) {
            Log.e("selectData", exception.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cardlist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.all -> {
                if (mode) {  // TEST MOOD
                    val intent = Intent(applicationContext, MakeTest::class.java)
                    intent.putExtra("moodAll", true)
                    intent.putExtra("folderId", folderId)
                    intent.putExtra("folderName", folderName)
                    intent.putExtra("flag", mark)
                    intent.putExtra("random", random)
                    startActivity(intent)
                }else {  // Not TEST
                    val intent = Intent(applicationContext, Learning::class.java)
                    intent.putExtra("moodAll", true)
                    intent.putExtra("folderId", folderId)
                    intent.putExtra("folderName", folderName)
                    intent.putExtra("flag", mark)
                    startActivity(intent)

                }

                true
            }
            R.id.add -> {
                val dialogFragment = CardAddDialog()
                dialogFragment.show(supportFragmentManager, "CardAddDialog")

                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }


        }
    }
    //フォルダーの新規作成
    private fun newCard(text: String) {
        val db = _helper.writableDatabase
        val sqlInsert = "INSERT INTO CardNameTable (id, card_name) Values(?, ?)"
        val stmt =db.compileStatement(sqlInsert)
        stmt.bindLong(1, folderId.toLong())
        stmt.bindString(2, text)
        stmt.executeInsert()

        onShow()
    }

    //フォルダー名の更新
    private fun updateCard(cid: String, newName: String) {
        try {
            val db = _helper.writableDatabase

            val values = ContentValues()
            values.put("card_name", newName)
            val whereClauses = "card_id = ?"
            val whereArgs = arrayOf(cid)
            db.update("CardNameTable", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }

    //フォルダーの削除
    private fun deleteCard(cid: String) {
        try {
            val db = _helper.writableDatabase
            val whereClauses = "card_id = ?"
            val whereArgs = arrayOf(cid)
            db.delete( "FlashcardTable", whereClauses, whereArgs)
            db.delete( "CardNameTable", whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData", exception.toString())
        }
    }

    //ダイアログからフォルダー名を受け取る
    override fun onDialogTextRecieve(dialog: DialogFragment, text: String) {
        //値を受け取る
        newCard(text)
        Log.d("dialog",text)
    }

    //メニューがタップされた時の操作
    override fun menuTapped(cid: String) {
        val item = arrayOf("名前の変更", "削除")
        AlertDialog.Builder(this)
            .setItems(item) { _, which ->
                if (which == 0) {
                    val dialog2 = AlertDialog.Builder(this)
                    val inflater = this.layoutInflater
                    val cview = inflater.inflate(R.layout.dialog_add_card, null)
                    dialog2.setView(cview)
                        .setTitle("card name?")
                        .setPositiveButton(R.string.change) { _, _ ->
                            val text = cview?.findViewById<EditText>(R.id.c_name)?.text
                            if (!text.isNullOrEmpty()) {
                                updateCard(cid, text.toString())
                                onShow()
                            }
                        }
                        .show()
                }
                if (which == 1) {
                    AlertDialog.Builder(this)
                        .setMessage("本当に削除しますか？")
                        .setPositiveButton("はい") { _, _ ->
                            deleteCard(cid)
                            onShow()
                        }
                        .setNeutralButton("いいえ") { _, _ ->

                        }
                        .show()
                }
            }
            .show()
    }

    //カードがタップされた時の操作
    override fun cardTapped(cid: String, cname: String) {
        if (mode) {  // TEST MOOD
            val intent = Intent(applicationContext, MakeTest::class.java)
            intent.putExtra("moodAll", false)
            intent.putExtra("cardId", cid)
            intent.putExtra("cardName", cname)
            intent.putExtra("folderId", folderId)
            intent.putExtra("flag", mark)
            intent.putExtra("random", random)
            startActivity(intent)
        }else {  // Not TEST
            val intent = Intent(applicationContext, Learning::class.java)
            intent.putExtra("moodAll", false)
            intent.putExtra("cardId", cid)
            intent.putExtra("cardName", cname)
            intent.putExtra("flag", mark)
            startActivity(intent)

        }
    }

    override fun imageTapped(cid: String, cname: String) {
        val item = arrayOf("from file", "add manually")
        AlertDialog.Builder(this)
            .setItems(item) { _, which ->
                if (which == 0) {
                    val files = File(path).listFiles()
                    val items: Array<String?> = arrayOfNulls(files.count())
                    var fileName = ""
                    var filePath = ""
                    for (i in 1 .. files.count()) {
                        if (files[i - 1].isFile && files[i - 1].name.endsWith(".csv")) {
                            items[i - 1] = files[i - 1].name
                        }
                        AlertDialog.Builder(this, R.style.selectFileDialog)
                            .setTitle("which file?($cname)")
                            .setSingleChoiceItems(items, -1) {_, w ->
                                fileName = items[w].toString()
                                filePath = "${this.path}/$fileName"
                            }
                            .setPositiveButton("import") {dialog, _ ->
                                if(fileName.isNotEmpty()) {
                                    registerCard(filePath, cid)
                                }
                                dialog.dismiss()
                            }
                            .setNeutralButton("cancel") {dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()


                    }
                }
                if (which == 1) {
                    val inflater = this.layoutInflater
                    val fView = inflater.inflate(R.layout.dialog_add_flash, null)
                    AlertDialog.Builder(this)
                        .setView(fView)
                        .setPositiveButton("save") {dialog, _ ->
                            val tag = fView?.findViewById<EditText>(R.id.tag)?.text
                            val question = fView?.findViewById<EditText>(R.id.question)?.text
                            val answer = fView?.findViewById<EditText>(R.id.answer)?.text
                            val explanation = fView?.findViewById<EditText>(R.id.explanation)?.text

                            if (!(question.isNullOrEmpty() || answer.isNullOrEmpty() || explanation.isNullOrEmpty())) {
                                if (!tag.isNullOrEmpty()) {
                                    registerManually(cid, tag.toString(), question.toString(), answer.toString(), explanation.toString())
                                }
                                else {
                                    registerManually(cid, null, question.toString(), answer.toString(), explanation.toString())
                                }
                            }
                            else {
                                Toast.makeText(this, "is canceled",Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }
                        .setNeutralButton("cancel") {dialog, _ ->
                            Toast.makeText(this, "is canceled",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            .show()
    }

    //登録(手動)
    private fun registerManually(cid: String, t: String?, q: String, a: String, e: String) {
        try {
            val db = _helper.writableDatabase
            val sqlInsert =
                "INSERT INTO FlashcardTable (id, card_id, tag, question, answer, explanation) Values(?,?,?,?,?,?)"
            val stmt = db.compileStatement(sqlInsert)
            stmt.bindLong(1, folderId.toLong())
            stmt.bindLong(2, cid.toLong())
            if (t != null) {
                stmt.bindLong(3, t.toLong())
            }else stmt.bindNull(3)
            stmt.bindString(4, q)
            stmt.bindString(5,  a)
            stmt.bindString(6,  e)
            stmt.executeInsert()
            Toast.makeText(this, "succeed!", Toast.LENGTH_SHORT).show()
        }catch (e: java.lang.Exception) {
            Toast.makeText(this, "failed! $e",Toast.LENGTH_LONG).show()
            Log.e("deleteData", e.toString())
        }
    }

    //登録(ファイルから)
    private fun registerCard(path: String, cid: String) {
        try {
            val file = File(path)
            val fileReader = FileReader(file)
            val bufferedReader = BufferedReader(fileReader)

            val db = _helper.writableDatabase
            var i = 0
            bufferedReader.forEachLine {
                if (it.isNotBlank()) {
                    if (i != 0) {
                        val line = it.split(",").toTypedArray()
                        val sqlInsert = "INSERT INTO FlashcardTable (id, card_id, tag, question, answer, explanation) Values(?,?,?,?,?,?)"
                        val stmt =db.compileStatement(sqlInsert)
                        stmt.bindLong(1, folderId.toLong())
                        stmt.bindLong(2, cid.toLong())
                        stmt.bindLong(3, line[1].toLong())
                        stmt.bindString(4,line[2])
                        stmt.bindString(5, line[3])
                        stmt.bindString(6, line[4])
                        stmt.executeInsert()
                        Log.d("fsf", line[3])
                    }
                }
                i += 1
            }
            Toast.makeText(this, "succeed!",Toast.LENGTH_SHORT).show()
        }catch (e: Exception) {
            Toast.makeText(this, "failed!$e",Toast.LENGTH_SHORT).show()
            Log.e("deleteData", e.toString())
        }
    }


    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
