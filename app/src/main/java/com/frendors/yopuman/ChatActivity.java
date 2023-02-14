package com.frendors.yopuman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaCodec;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.frendors.yopuman.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends Activity  implements View.OnClickListener  {

    Intent intent;
    Button btnCmd;
    Button btnPoem;
    Button btnGo;
    Button btnClosedialog;
    EditText editText;
    ListView list_chat;
    SimpleCursorAdapter scAdapter;
    LinearLayout bottom_blk;
    final int MAX_CHARACTERS = 150;
    final int MIN_CHARACTERS = 2;
  static  boolean canSend;
  static String current_msg;
    String result;
   final String URL = "https://frendors.com/bot/app/index.php";
    DBHelperChat dbHelper;
    SQLiteDatabase db;
    Cursor c;
    SharedPreferences prefs;
     AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbHelper = new DBHelperChat(this);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = dbHelper.getReadableDatabase();
        }

        list_chat = (ListView) findViewById(R.id.list_chat);
        btnCmd = (Button) findViewById(R.id.command_btn);
        btnPoem = (Button) findViewById(R.id.poem_btn);
        btnGo = (Button) findViewById(R.id.send_btn);
        bottom_blk = (LinearLayout) findViewById(R.id.bottom_blk);
        editText = (EditText) findViewById(R.id.edit_text);
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_CHARACTERS)});

        ShowChat();

        editText.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                // Прописываем то, что надо выполнить после изменения текста
                if((editText.getText().toString().length() < MIN_CHARACTERS) || Pattern.matches("^\\s*$",current_msg)){

                    canSend = false;

                }else {

                    canSend = true;

                }

                editText.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            SendMsq();
                            return true;
                        }
                        return false;
                    }
                });

                current_msg = editText.getText().toString();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

       btnCmd.setOnClickListener(this);
        btnPoem.setOnClickListener(this);
        btnGo.setOnClickListener(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChatActivity.this);

        LayoutInflater inflater = ChatActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_view, null);
        dialogBuilder.setView(dialogView);

        btnClosedialog = dialogView.findViewById(R.id.close_dialog_btn);

         alertDialog = dialogBuilder.create();

        btnClosedialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.hide();

            }
        });

    }

   @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.poem_btn:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.command_btn:
                 alertDialog.show();
                break;
            case R.id.send_btn:
                //TODO:отправить сообщение
                SendMsq();
                break;

        }

    }

    private void SendMsq(){

        current_msg = editText.getText().toString().replace("\t","").replace("\n","").trim();

        if(current_msg.length() < MIN_CHARACTERS || Pattern.matches("^\\s*$",current_msg)){

            if(current_msg.length() == 1){
                Toast.makeText(getApplicationContext(), "Мало символов нахуй!", Toast.LENGTH_LONG).show();
            }

            return;
        }

        SetEnabledButtons(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {

                        try {
                            JSONObject json = new JSONObject(response);

                            json.getString("response");
                            result = json.getString("response");

                            AddMSG(result,current_msg);
                            editText.setText("");

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Произошла хуйня", Toast.LENGTH_LONG).show();
                        }

                        SetEnabledButtons(true);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        SetEnabledButtons(true);
                        Toast.makeText(getApplicationContext(), "Произошла хуйня", Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("q", current_msg.replace("\t","").replace("\n","").trim());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
}
    private void SetEnabledButtons(boolean flag){

        btnCmd.setEnabled(flag);
        btnPoem.setEnabled(flag);

    }

    private void AddMSG(String result,String current_msg){

        // Создайте новую строку со значениями для вставки.
        ContentValues newValues = new ContentValues();
        // Задайте значения для каждой строки.
        newValues.put("q", current_msg);
        newValues.put("a", result);
        // Вставьте строку в вашу базу данных.
        db.beginTransaction();
        db.insert("chat", null, newValues);
        db.setTransactionSuccessful();
        db.endTransaction();

        ShowChat();

    }

    private void ShowChat(){

        c = db.query("chat", null, null, null, null, null, "_id desc");
        startManagingCursor(c);

        // формируем столбцы сопоставления
        String[] from = new String[] {"_id","q","a"};
        int[] to = new int[] { R.id._id, R.id.q_item, R.id.a_item};

        scAdapter = new SimpleCursorAdapter(this, R.layout.spinner_chat_item, c, from, to);
        list_chat.setAdapter(scAdapter);
        registerForContextMenu(list_chat);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() ==  MotionEvent.ACTION_DOWN) hideKeyboard();
        return super.dispatchTouchEvent(ev);
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
            db.delete("chat",   " _id= " + acmi.id, null);;
            // обновляем курсор
            c.requery();
            return true;
        }
        return super.onContextItemSelected(item);

    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

}