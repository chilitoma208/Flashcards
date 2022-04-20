package com.example.flashcards

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_Name, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_Name = "flashcards.db"
        private const val DATABASE_VERSION = 1

    }
    override fun onCreate(db: SQLiteDatabase) {
        with(db) {
            execSQL("create table FolderTable (id integer primary key autoincrement, folder_name text default 'a')")
            execSQL("create table CardNameTable (card_id integer primary key autoincrement, id integer, card_name text default 'b')")
            execSQL("create table FlashcardTable (f_id integer primary key, id integer, card_id integer, flag integer default 0, tag integer default 0, question text, answer text, explanation text)")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db.execSQL("alter table FolderTable add column deleteFlag integer default 0")
            db.execSQL("alter table CardNameTable add column deleteFlag integer default 0")
            db.execSQL("alter table FlashcardTable add column deleteFlag integer default 0")
        }
    }
}