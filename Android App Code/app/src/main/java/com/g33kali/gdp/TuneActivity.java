package com.g33kali.gdp;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class TuneActivity extends AppCompatActivity implements View.OnClickListener{

    Button btn1, btn2, btn3, btn4, btn5;

    ImageView btn_info, btn_connection;
    RequestQueue queue;
    //ESP32 aREST server address
    final String url = "http://192.168.7.1";
    boolean isConnected = false;
    String device = "GDP";
    int tuneMode= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tune);

        //set widget
         btn1 = findViewById(R.id.btn1);
         btn2 = findViewById(R.id.btn2);
         btn3 = findViewById(R.id.btn3);
         btn4 = findViewById(R.id.btn4);
         btn5 = findViewById(R.id.btn5);
        btn_connection = findViewById(R.id.btn_connection);

         //Set On Click Listener
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn_connection.setOnClickListener(this);

        queue = Volley.newRequestQueue(this);
        sendRequest();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.btn1:
                swicthMode(1);
                btn1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case R.id.btn2:
                swicthMode(2);
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case R.id.btn3:
                swicthMode(3);
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case R.id.btn4:
                swicthMode(4);
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn5:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case  R.id.btn_connection:
                displayDeviceInfo();
                break;

        }
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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
                            JSONObject variables = response.getJSONObject("variables");
                            Log.d("TEST2 ",variables.toString());
                            tuneMode = variables.getInt("tune_mode");
                            String deviceName = response.getString("name");
                            deviceName += response.getString("id");
                            device = deviceName;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // display response
                        Log.d("Response", response.toString());
                        setTuneMode();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isConnected = false;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi_off);
                        Log.d("Error.Response", error.toString());

                        new SweetAlertDialog(TuneActivity.this, SweetAlertDialog.WARNING_TYPE)
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
        );

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    void swicthMode(int mode){
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url+"/change_mode?params="+mode, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        isConnected = true;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi);
                        try {

                            tuneMode = response.getInt("return_value");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // display response
                        Log.d("Response", response.toString());
                        setTuneMode();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isConnected = false;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi_off);
                        Log.d("Error.Response", error.toString());

                        new SweetAlertDialog(TuneActivity.this, SweetAlertDialog.WARNING_TYPE)
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
        );

        // add it to the RequestQueue
        queue.add(getRequest);

    }

    void setTuneMode(){
        Log.d("Response", " "+tuneMode);
        switch(tuneMode) {
            case 1:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case 2:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case 3:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                break;
            case 4:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.btn5:
                swicthMode(5);
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                break;
            default:
                btn1.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn2.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn3.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                btn4.setBackgroundColor(getResources().getColor(R.color.colorOrange));
        }

    }

    //Show Connection details
    void displayDeviceInfo(){
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
