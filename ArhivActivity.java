package com.frendors.yopuman;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.frendors.yopuman.R;

import java.util.ArrayList;

public class ArhivActivity extends Activity implements View.OnClickListener {

    Button btnPoem;
    Button btnChat;

    Intent intent;
    TextView out_blk;
    ArrayList<String> poems = new  ArrayList<String>();
    DBHelper dbHelper;
    SQLiteDatabase db;

    // курсор
    Cursor c;

    ListView scroller;
    SimpleCursorAdapter scAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arhiv);

        btnPoem = (Button) findViewById(R.id.poem_btn);
        btnChat = (Button) findViewById(R.id.chat_btn);

        btnPoem.setOnClickListener(this);

        btnChat.setOnClickListener(this);
        scroller = (ListView) findViewById(R.id.scroller);

        out_blk = (TextView) findViewById(R.id.out_blk);

        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch (
                SQLiteException ex){
            db = dbHelper.getReadableDatabase();
        }

        ShowPoems();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.poem_btn:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.chat_btn:
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                break;

        }

    }

    private void ShowPoems(){

        // получаем курсор
        c = db.query("poems", null, null, null, null, null, "_id desc");;
        startManagingCursor(c);

        // формируем столбцы сопоставления
        String[] from = new String[] {"_id","txt"};
        int[] to = new int[] { R.id._id, R.id.spinner_poem_item };

        // создааем адаптер и настраиваем список
        scAdapter = new SimpleCursorAdapter(this, R.layout.spinner_poem_item, c, from, to);
        scroller = (ListView) findViewById(R.id.scroller);
        scroller.setAdapter(scAdapter);
        registerForContextMenu(scroller);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, R.string.delete_lbl);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            db.delete("poems",   " _id= " + acmi.id, null);;
            // обновляем курсор
            c.requery();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

}


