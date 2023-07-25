package com.frendors.yopuman;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.android.volley.VolleyLog.TAG;

public class DBHelperChat extends SQLiteOpenHelper {

    public DBHelperChat(Context context) {
        // конструктор суперкласса
        super(context, "DBCHAT", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- ТАБЛИЦА СООБЩЕНИЙ СОЗДАНА ---");
        // создаем таблицу с полями
        db.execSQL ( "create table if not exists chat ("
                + "_id integer primary key autoincrement,"
                + "q text not null," +
                "a text not null);"  );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
