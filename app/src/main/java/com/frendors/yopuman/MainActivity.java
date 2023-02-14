package com.frendors.yopuman;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class MainActivity extends Activity implements View.OnClickListener {

    TextView out_blk;
    Button btnZs;
    Button btnPianka;
    Button btnBv;
    Button btnKbd;
    Button btnFull;
    LinearLayout bottomBlk;
    Button btnSave;
    Button btnArhiv;
    Button btnChat;
    Intent intent;

  static  String current_poem = "";
    String current_btn = "";
    String result = "";
    String URL = "https://frendors.com/bot/app/index.php";
DBHelper dbHelper;
SQLiteDatabase db;
SharedPreferences prefs;

static boolean isSaved;

    public static final String APP_PREFERENCES_LASTPOEM = "last_poem"; // имя кота
    public static final String APP_PREFERENCES_ISSAVED = "isSaved"; // возраст кота

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dbHelper = new DBHelper(this);
        try {
            db = dbHelper.getWritableDatabase();
        }
        catch (SQLiteException ex){
            db = dbHelper.getReadableDatabase();
        }

        btnZs = (Button) findViewById(R.id.zs_btn);
        btnPianka = (Button) findViewById(R.id.pianka_btn);
        btnBv = (Button) findViewById(R.id.bv_btn);
        btnKbd = (Button) findViewById(R.id.kbd_btn);
        btnFull = (Button) findViewById(R.id.full_btn);
        btnArhiv = (Button) findViewById(R.id.arhiv_btn);
        btnSave = (Button) findViewById(R.id.save_btn);
        bottomBlk = (LinearLayout) findViewById(R.id.bottom_blk);
        btnChat = (Button) findViewById(R.id.chat_btn);

        prefs = getSharedPreferences("prefs",Context.MODE_PRIVATE);

        isSaved = prefs.getBoolean(APP_PREFERENCES_ISSAVED,false);

        CheckSaveBtnActive();

        out_blk = (TextView) findViewById(R.id.out_blk);

        // присваиваем обработчик кнопкам
        btnZs.setOnClickListener(this);
        btnPianka.setOnClickListener(this);
        btnBv.setOnClickListener(this);
        btnKbd.setOnClickListener(this);
        btnFull.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnArhiv.setOnClickListener(this);
        btnChat.setOnClickListener(this);

        if(CheckPoemsCount() > 0){
            btnArhiv.setEnabled(true);
        }

        if(prefs.getString(APP_PREFERENCES_LASTPOEM,"").length() == 0){}

        btnSave.setEnabled(false);

    }

    @Override
    protected void onStart(){
        super.onStart();
        out_blk.setText(prefs.getString(APP_PREFERENCES_LASTPOEM,""));
        CheckSaveBtnActive();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        CheckSaveBtnActive();

    }

    @Override
    public void onClick(View v) {
        // по id определеяем кнопку, вызвавшую этот обработчик
        boolean sendData = true;
        switch (v.getId()) {
            case R.id.pianka_btn:
                current_btn = "pianka";
                break;
            case R.id.zs_btn:
                current_btn = "zs";
                break;
            case R.id.bv_btn:
                       current_btn = "5bv";
                  break;
            case R.id.kbd_btn:
                    current_btn = "kbd";
                break;
            case R.id.full_btn:
                      current_btn = "full";break;
            case R.id.save_btn:
               SavePoem(); sendData = false; break;
            case R.id.arhiv_btn:
                 sendData = false;
            intent = new Intent(this, ArhivActivity.class);
            startActivity(intent);  break;
            case R.id.chat_btn:
                sendData = false;
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);  break;
            default:sendData = false;break;
        }

if(sendData){

    try {

        SendData(URL,current_btn);

    } catch (Exception e) {

    }

}
    }

    private void SendData(final String URL, final String q)
    {

        SetEnabledButtons(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {

                        try {

                            JSONObject json = new JSONObject(response);
                            result = json.getString("poem");
                            out_blk.setText(result);
                            btnSave.setEnabled(true);

                            isSaved = false;

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(APP_PREFERENCES_LASTPOEM, result);
                            editor.apply();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

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
                params.put("q", q);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void SavePoem(){

        Runnable runnable = new Runnable() {
            public void run() {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("save", "1");
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    Handler handler = new Handler(new Handler.Callback() {
           @Override
            public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String status = bundle.getString("save");

            if(status.equals("1")){

                btnSave.setEnabled(false);
                btnArhiv.setEnabled(false);
                btnChat.setEnabled(false);

                // Создайте новую строку со значениями для вставки.
                ContentValues newValues = new ContentValues();
                // Задайте значения для каждой строки.
                newValues.put("txt", out_blk.getText().toString());
                // Вставьте строку в вашу базу данных.
                db.beginTransaction();

                db.insert("poems", null, newValues);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(APP_PREFERENCES_ISSAVED, true);
                editor.apply();
                isSaved = true;

                Toast.makeText(getApplicationContext(), "Стишок добавлен!", Toast.LENGTH_LONG).show();
                db.setTransactionSuccessful();
                db.endTransaction();

                btnArhiv.setEnabled(true);
                btnChat.setEnabled(true);

                return true;

            }else{

                return false;
            }


    }
    });

    private void SetEnabledButtons(boolean flag){

        btnZs.setEnabled(flag);
        btnPianka.setEnabled(flag);
        btnBv.setEnabled(flag);
        btnKbd.setEnabled(flag);
        btnFull.setEnabled(flag);
        btnSave.setEnabled(flag);

    }

    private long CheckPoemsCount(){
        // курсор
        Cursor c = null;
        c = db.query("poems", null, null, null, null, null, null);

       int count = c.getCount();
       c.close();
        return count;
    }

    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(APP_PREFERENCES_ISSAVED, isSaved);
        editor.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        current_poem = out_blk.getText().toString();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(APP_PREFERENCES_ISSAVED, isSaved);
        editor.apply();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        out_blk.setText(prefs.getString(APP_PREFERENCES_LASTPOEM,""));
        CheckSaveBtnActive();

    }

    private void CheckSaveBtnActive(){

        try{

            if(getSharedPreferences("prefs",Context.MODE_PRIVATE).contains(APP_PREFERENCES_ISSAVED)) {

                if(getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean(APP_PREFERENCES_ISSAVED,false) == true){
                    btnSave.setEnabled(false);
                } else{

                    btnSave.setEnabled(true);

                }

            }

        }catch(Exception ex){

        }

    }

}


