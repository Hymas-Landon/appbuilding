package com.g33kali.gdp;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.Timer;
import java.util.TimerTask;

public class LiveDataActivity extends AppCompatActivity {

    TextView tvBoost, tvEgt, tvOilPressure ,tvFule, tvTrubo, tvDfrp, tvTiming, tvCoolant, tvGear, tvAfrp,tvTune;
    ImageView btn_info, btn_connection;
    RequestQueue queue;
    //ESP32 aREST server address
    final String url = "http://192.168.7.1";
    boolean isConnected = false;
    boolean isProcessing = false;

    String device = "GDP";
    int tuneMode= 0;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_live_data);

        btn_connection = findViewById(R.id.btn_connection);
        tvBoost = findViewById(R.id.tv_boost);
        tvEgt = findViewById(R.id.tv_egt);
        tvOilPressure = findViewById(R.id.tv_oil_pressure);
        tvFule = findViewById(R.id.tv_fule);
        tvTrubo = findViewById(R.id.tv_turbo);
        tvDfrp = findViewById(R.id.tv_dfrp);
        tvTiming = findViewById(R.id.tv_timing);
        tvCoolant = findViewById(R.id.tv_coolant);
        tvGear = findViewById(R.id.tv_gear);
        tvAfrp = findViewById(R.id.tv_afrp);
        tvTune = findViewById(R.id.tv_tune);

        queue = Volley.newRequestQueue(this);
        sendRequest();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int num = 1;

            @Override
            public void run() {
                if(isConnected){
                    if(!isProcessing){
                        Log.d("TEST2 :","Sending request");
                        updateRequest();
                    }
                }

            }
        }, 0, 500);//put here time 1000 milliseconds=1 second


    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int num = 1;

            @Override
            public void run() {
                if(isConnected){
                    if(!isProcessing){
                        Log.d("TEST2 :","Sending request");
                        updateRequest();
                    }
                }

            }
        }, 0, 500);//put here time 1000 milliseconds=1 second
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
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isConnected = false;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi_off);
                        Log.d("Error.Response", error.toString());

                        new SweetAlertDialog(LiveDataActivity.this, SweetAlertDialog.WARNING_TYPE)
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

    //Send to sGDP server to get live data
    public void updateRequest(){
        isProcessing = true;
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
                            tvTune.setText("Tune :"+tuneMode);
                            tvBoost.setText(variables.getString("boost"));
                            tvEgt.setText(variables.getString("egt"));
                            tvFule.setText(variables.getString("fule"));
                            tvOilPressure.setText(variables.getString("oil_pressur"));
                            tvTrubo.setText(variables.getString("turbo"));
                            tvDfrp.setText(variables.getString("frp"));
                            tvTiming.setText(variables.getString("timing"));
                            tvCoolant.setText(variables.getString("coolant"));
                            tvGear.setText(variables.getString("gear"));
                            tvAfrp.setText(variables.getString("frp"));

                            Log.d("Response", response.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // display response

                        isProcessing = false;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isConnected = false;
                        btn_connection.setImageResource(R.drawable.ic_action_portable_wifi_off);
                        Log.d("Error.Response", error.toString());

                        new SweetAlertDialog(LiveDataActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                        isProcessing = false;
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);

    }


}
