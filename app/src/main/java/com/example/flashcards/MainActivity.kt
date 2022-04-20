package com.example.flashcards

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), FolderAddDialog.DialogListener, FolderMenuListener {
    private var arrayListId: ArrayList<Int> = arrayListOf()
    private var arrayListName: ArrayList<String> = arrayListOf()
    private val _helper = DatabaseHelper(this@MainActivity)

    lateinit var myAdapter: FolderAdapter
    var list: ArrayList<MyFolder> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        onShow()
    }

    protected fun onShow() {
        try {
            arrayListId.clear()
            arrayListName.clear()
            val db = _helper.readableDatabase
            // データベースを検索
            val sql = "SELECT id, folder_name FROM folderTable"
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
                list.add(i - 1, MyFolder(
                    arrayListId[i - 1],
                    arrayListName[i - 1]
                ))
            }

            val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)
            myAdapter = FolderAdapter(list, this)
            recyclerView.adapter = myAdapter



        }catch(exception: Exception) {
            Log.e("selectData", exception.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            R.id.add -> {
                val dialogFragment = FolderAddDialog()
                dialogFragment.show(supportFragmentManager, "FolderAddDialog")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }


        }
    }

    //フォルダーの新規作成
    fun newFolder(text: String) {
        val db = _helper.writableDatabase
        val sqlInsert = "INSERT INTO FolderTable (folder_name) Values(?)"
        val stmt =db.compileStatement(sqlInsert)
        stmt.bindString(1, text)
        stmt.executeInsert()
        onShow()
    }

    //フォルダー名の更新
    private fun updateFolder(fid: String, newName: String) {
        try {
            val db = _helper.writableDatabase

            val values = ContentValues()
            values.put("folder_name", newName)

            val whereClauses = "id = ?"
            val whereArgs = arrayOf(fid)
            db.update("FolderTable", values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }

    //フォルダーの削除
    private fun deleteFolder(fid: String) {
        try {
            val db = _helper.writableDatabase
            val whereClauses = "id = ?"
            val whereArgs = arrayOf(fid)
            db.delete("FlashcardTable", whereClauses, whereArgs)
            db.delete("CardNameTable", whereClauses, whereArgs)
            db.delete("FolderTable", whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData", exception.toString())
        }
    }

    //ダイアログからフォルダー名を受け取る
    override fun onDialogTextRecieve(dialog: DialogFragment, text: String) {
        //値を受け取る
        newFolder(text)
    }

    //フォルダーがタップされた時の動作(次の画面へ移動)
    override fun folderTapped(fid: String, fname: String) {
        val intent = Intent(applicationContext, CardList::class.java)
        intent.putExtra("folderId", fid)
        intent.putExtra("folderName", fname)
        startActivity(intent)
    }
    //メニューがタップされた時の動作
    override fun menuTapped(fid: String) {
        val item = arrayOf("名前の変更", "削除")
        AlertDialog.Builder(this)
            .setItems(item) { _, which ->
                if (which == 0) {
                    val dialog2 =AlertDialog.Builder(this)
                    val inflater = this.layoutInflater
                    val s_View = inflater.inflate(R.layout.dialog_add_folder, null)
                    dialog2.setView(s_View)
                        .setTitle("folder name?")
                        .setPositiveButton(R.string.change) { _, _ ->
                            val text = s_View?.findViewById<EditText>(R.id.f_name)?.text
                            if (!text.isNullOrEmpty()) {
                                updateFolder(fid, text.toString())
                                onShow()
                            }
                        }
                        .show()
                }
                if (which == 1) {
                    AlertDialog.Builder(this)
                        .setMessage("本当に削除しますか？")
                        .setPositiveButton("はい") { _, _ ->
                            deleteFolder(fid)
                            onShow()
                        }
                        .setNeutralButton("いいえ") { _, _ ->

                        }
                        .show()
                }
            }
            .show()
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
