package com.pygabo.rabbfinance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pygabo.rabbfinance.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class      LoginActivity extends AppCompatActivity {
    RequestQueue requestQueue;
    JsonObjectRequest jsArrayRequest;

    EditText mUser;
    EditText mPassword;
    ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1);
        runUserSave();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUser = (EditText) findViewById(R.id.mUser);
        mPassword = (EditText) findViewById(R.id.mPassword);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress = ProgressDialog.show(view.getContext(), "Login ...",
                        "Please wait...", true);
                if (len(mUser) && len(mPassword)) {

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    Login();
                                }
                            },
                            3000);
                    Snackbar.make(view, "Login!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    toast("Please, complete all fields");
                }


            }
        });
    }

    public void Login(){

        String url = Constants.CURRENT_URL+"/rest-auth/login/";
        requestQueue= Volley.newRequestQueue(getApplicationContext());

        HashMap<String, String> parametros = new HashMap();
        parametros.put("username", mUser.getText().toString());
        parametros.put("password", mPassword.getText().toString());

        jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                new JSONObject(parametros),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        postLogin(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        Log.d("Error", "Error Respuesta en JSON: " + error.getMessage());
                    }
                }
        );

        requestQueue.add(jsArrayRequest);
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                progress.dismiss();
            }
        });
    }

    public void postLogin(JSONObject response){
        String token = null;
        try {
            token = response.getString("key");
            if (token != null){
                writeSessionPreferences(token);
                runMain();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean len(EditText m) {
        return m.getText().toString().length() > 0;
    }

    public void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    public void runUserSave(){
        SharedPreferences sharedPref = getSharedPreferences("session" ,Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", "");
        if (!token.equalsIgnoreCase(""))
            this.runMain();
    }

    public void writeSessionPreferences(String token){
        SharedPreferences sharedPref = getSharedPreferences("session" ,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token).apply();
    }

    public void runMain(){
        Intent i = new Intent(LoginActivity.this, TodoActivity.class);
        SharedPreferences sharedPref = getSharedPreferences("session" ,Context.MODE_PRIVATE);
        i.putExtra("token", sharedPref.getString("token", ""));
        startActivity(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if ( ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        ) {
                    finish();
                }
            }
        }
    }


}
