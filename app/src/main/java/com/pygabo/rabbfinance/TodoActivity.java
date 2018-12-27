package com.pygabo.rabbfinance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pygabo.rabbfinance.utils.ClickHandler;
import com.pygabo.rabbfinance.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class TodoActivity extends AppCompatActivity {

    EditText mDescription;
    RequestQueue requestQueue, TaskrequestQueue;
    JsonObjectRequest jsArrayRequest, TaskjsArrayRequest;
    private List<TodoItem> todoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TodoAdapter todoAdapter;
    public static boolean isAppRunning = true;
    private View parentLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseRequestObjectDevice();
        setContentView(R.layout.activity_todo);
        parentLayout = findViewById(android.R.id.content);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "1";
        String channel2 = "2";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    "Channel 1",NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("This is BNT");
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationChannel notificationChannel2 = new NotificationChannel(channel2,
                    "Channel 2",NotificationManager.IMPORTANCE_MIN);

            notificationChannel.setDescription("This is bTV");
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel2);
        }

        setUserData();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.todos_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(todoAdapter);
        setAdapter();
        setDataAdapter();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTodoAlert();
            }
        });

    }

    public void setAdapter() {
        todoAdapter = new TodoAdapter(todoList, new ClickHandler() {
            @Override
            public void onMyButtonClicked(int position) {
                TodoItem todo = todoAdapter.getItem(position);
                setTaskReady(todo.getId());
            }
        });
    }

    public void setDataAdapter() {
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constants.CURRENT_URL + "/api/app/viewsets/to-do/",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        todoList = parseJson(response);
                        setAdapter();
                        recyclerView.setAdapter(todoAdapter);
                        todoAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + getTokenUser());
                return headers;
            }
        };

        requestQueue.add(jsArrayRequest);
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {

            }
        });
    }


    public List<TodoItem> parseJson(JSONObject jsonObject) {
        // Variables locales
        List<TodoItem> todos = new ArrayList();
        JSONArray jsonArray = null;

        try {
            // Obtener el array del objeto
            jsonArray = jsonObject.getJSONArray("todos");

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject objeto = jsonArray.getJSONObject(i);


                    todos.add(new TodoItem(
                            Integer.parseInt(objeto.getString("id")),
                            objeto.getString("description"),
                            objeto.getBoolean("ready")
                    ));

                } catch (JSONException e) {
                    Log.e(TAG, "Error de parsing: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return todos;
    }

    public void showAddTodoAlert() {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View alertView = inflater.inflate(R.layout.dialog_todo, null);
        builder.setView(alertView);
        mDescription = (EditText) alertView.findViewById(R.id.todo_description);
        builder.setTitle("Add Todo");
        mDescription.setHint("write someting...");

        mDescription.requestFocus();

        builder.setCancelable(false);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String todo = mDescription.getText().toString();
                if (todo.length() > 0) {
                    addTodo(todo);

                } else {
                    toast("Error, write someting...");
                    showAddTodoAlert();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    public void addTodo(String description) {
        HashMap<String, String> params = new HashMap();
        params.put("description", description);

        requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Constants.CURRENT_URL + "/api/app/viewsets/to-do/",
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        toast("success!!");
                        setDataAdapter();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        toast("Error creating");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + getTokenUser());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public String getTokenUser(){
        SharedPreferences sharedPref = getSharedPreferences("session" , Context.MODE_PRIVATE);
        return sharedPref.getString("token", "");
    }

    public void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    public void setUserData(){
        String url = Constants.CURRENT_URL+"/rest-auth/user/";
        requestQueue= Volley.newRequestQueue(getApplicationContext());

        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        BindXmlUserData(response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Error USer null: " + error.getMessage());


                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token "+getTokenUser());
                return headers;
            }
        };

        requestQueue.add(jsArrayRequest);
    }

    public void parseRequestObjectDevice() {
        String myIMEI = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        HashMap<String, Object> parametros = new HashMap();
        parametros.put("name", Build.MODEL);
        parametros.put("device_id",  myIMEI);
        parametros.put("reg_id",  FirebaseInstanceId.getInstance().getToken());
        parametros.put("is_active", true);
        Log.e("request", "hastmap "+parametros);
        sendDeviceItem(parametros);
    }


    public void sendDeviceItem(HashMap<String, Object> request_object){
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Constants.CURRENT_URL + "/fcm/devices/",
                new JSONObject(request_object),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + getTokenUser());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void BindXmlUserData(JSONObject response){
        int user_id;
        try {
            user_id = Integer.parseInt(response.getString("pk"));
            setRequestUserId(user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRequestUserId(int user_id){
        SharedPreferences sharedPref = getSharedPreferences("session" ,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("usuario", user_id).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
    }

    public void setTaskReady(final int id) {
        TaskrequestQueue = Volley.newRequestQueue(this);
        TaskrequestQueue.getCache().clear();
        TaskjsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                Constants.CURRENT_URL + "/api/app/viewsets/to-do/"+id+"/ready/",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Snackbar.make(parentLayout, "set task status", Snackbar.LENGTH_LONG)
                                .setAction("CANCEL", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        setTaskReady(id);
                                        setDataAdapter();
                                    }
                                })
                                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                .show();
                        setDataAdapter();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + getTokenUser());
                return headers;
            }
        };

        TaskrequestQueue.add(TaskjsArrayRequest);
        TaskrequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {

            }
        });
        setDataAdapter();
    }

}
