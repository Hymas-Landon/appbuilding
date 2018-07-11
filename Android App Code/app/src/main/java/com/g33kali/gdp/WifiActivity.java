package com.g33kali.gdp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class WifiActivity extends AppCompatActivity {
    TextView connectedDevice;
    mehdi.sakout.fancybuttons.FancyButton btn_change;
    EditText input_ssid, input_pwd;
    RequestQueue queue;

    //ESP32 aREST server address
    final String url = "http://192.168.7.1";
    String device = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Wifi Settings");

        connectedDevice = findViewById(R.id.text_device);
        input_ssid = findViewById(R.id.input_ssid);
        input_pwd = findViewById(R.id.input_password);

        btn_change = findViewById(R.id.btn_change_settings);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChangeWifi();
            }
        });

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

    //Send to sGDP server to verify connection
    public void sendChangeWifi(){
        // prepare the Request
        String ssid = input_ssid.getText().toString();
        String pwd = input_pwd.getText().toString();
        if (ssid.isEmpty()){
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Opps...")
                    .setContentText("SSID Can't be empty")
                    .setConfirmText("ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            // reuse previous dialog instance
                            sDialog.dismiss();
                        }
                    })
                    .show();
            return;
        }
        if(pwd.length() < 8){
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Opps...")
                    .setContentText("password should be atleast 8 character long")
                    .setConfirmText("ok")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            // reuse previous dialog instance
                            sDialog.dismiss();
                        }
                    })
                    .show();
        }else{

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url+"/change_wifi?params="+ssid+":"+pwd, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int return_value = response.getInt("return_value");
                                if (return_value == 1){
                                    new SweetAlertDialog(WifiActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Wifi Settings Changed Successfully.")
                                            .setConfirmText("ok")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sDialog) {
                                                    // reuse previous dialog instance
                                                    sDialog.dismiss();
                                                }
                                            })
                                            .show();
                                }else{
                                   showError();
                                }
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
                            device ="";
                            connectedDevice.setText(device);
                            showError();
                            Log.d("Error.Response", error.toString());
                        }
                    }
            );

            // add it to the RequestQueue
            queue.add(getRequest);
        }


    }

    public void sendRequest(){

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String deviceName = response.getString("name");
                            deviceName += response.getString("id");
                            device = deviceName;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        connectedDevice.setText(device);
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        device ="";
                        connectedDevice.setText(device);
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);

    }
   void showError(){
       new SweetAlertDialog(WifiActivity.this, SweetAlertDialog.SUCCESS_TYPE)
               .setTitleText("Error!")
               .setContentText("Spmething Wrong Retry.")
               .setConfirmText("ok")
               .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                   @Override
                   public void onClick(SweetAlertDialog sDialog) {
                       // reuse previous dialog instance
                       sDialog.dismiss();
                   }
               })
               .show();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
