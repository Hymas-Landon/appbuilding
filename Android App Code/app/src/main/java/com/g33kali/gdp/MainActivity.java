package com.g33kali.gdp;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView btn_info, btn_connection;
    Button btn_tune, btn_live;
    RequestQueue queue;
    //ESP32 aREST server address
    final String url = "http://192.168.7.1";
    boolean isConnected = false;
    String device = "GDP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //Set widgets
        btn_tune = findViewById(R.id.btn_tune);
        btn_live = findViewById(R.id.btn_live_data);
        btn_info = findViewById(R.id.btn_info);
        btn_connection = findViewById(R.id.btn_connection);

        //Set OnClick Listener
        btn_tune.setOnClickListener(this);
        btn_live.setOnClickListener(this);
        btn_info.setOnClickListener(this);
        btn_connection.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);

        sendRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendRequest();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        sendRequest();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       if (id == R.id.action_wifi) {
           startActivity(new Intent(MainActivity.this, WifiActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch(id){
            case R.id.btn_tune:
                startActivity(new Intent(MainActivity.this, TuneActivity.class));
                break;
            case R.id.btn_live_data:
                startActivity(new Intent(MainActivity.this, LiveDataActivity.class));
                break;
            case  R.id.btn_info:
                Toast.makeText(MainActivity.this, "Info Button Clicked",Toast.LENGTH_SHORT).show();
                break;
            case  R.id.btn_connection:
                displayDevicecInfo();
                break;
        }


    }

    //Send to sGDP server to verify connection
    public void sendRequest(){
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        isConnected = true;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi);
                        try {
                            String deviceName = response.getString("name");
                            deviceName += response.getString("id");
                            device = deviceName;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isConnected = false;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi_off);
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    //Show Connection details
    void displayDevicecInfo(){
        if (isConnected){
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Connected")
                    .setContentText("You are connected to "+device)
                    .setConfirmText("ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            // reuse previous dialog instance
                            sDialog.dismiss();
                        }
                    })
                    .show();
        }else {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("No Connection")
                    .setContentText("Your are not connected to GDP device")
                    .setCancelText("Retry")
                    .setConfirmText("Connect")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sendRequest();
                            sDialog.dismiss();
                        }
                    })
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .show();
        }

    }
}


