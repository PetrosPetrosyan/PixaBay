package com.example.pet.pixabay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private EditText search_text;
    private Button search_button;
    private String search;
    private HashMap<String, Integer> map = new HashMap<>();
    private GridView gridview;
    private ArrayList<String> mThumbIds;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        search_text = findViewById(R.id.search_text);
        search_button = findViewById(R.id.search_button);
        search = "";
        ;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("NO Internet Conection!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.cancel();
                }
            });
            alertDialog.show();

        }


        mThumbIds = new ArrayList<>();
        getUrlStrings();
        gridview = findViewById(R.id.grid_view);
        ImageAdapter imageAdapter = new ImageAdapter(this, mThumbIds);
        gridview.setAdapter(imageAdapter);
        int screenWidth = (MainActivity.this).getWindowManager()
                .getDefaultDisplay().getHeight();
        gridview.getLayoutParams().height = screenWidth;

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search = search_text.getText().toString();
                mThumbIds = new ArrayList<>();
                getUrlStrings();
                ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, mThumbIds);
                gridview.setAdapter(imageAdapter);
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), FullImage.class);
                intent.putExtra("position", map.get(mThumbIds.get(position)));
                startActivity(intent);

            }
        });

//        search_text.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                search = search_text.getText().toString();
//                mThumbIds = new ArrayList<>();
//                getUrlStrings();
//                ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, mThumbIds);
//                gridview.setAdapter(imageAdapter);
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
        swipeRefreshLayout = findViewById(R.id.swipelayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        mThumbIds = new ArrayList<>();
                        getUrlStrings();
                        ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, mThumbIds);
                        gridview.setAdapter(imageAdapter);

                    }
                }, 500);
            }
        });


    }

    public void getUrlStrings() {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get("https://pixabay.com/api/?key=10854982-e556022b1b07997f06e504502&q=yellow+flowers&image_type=photo&pretty=true", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray jsonArray = response.getJSONArray("hits");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String mThumbID = jsonObject.getString("webformatURL");
                        map.put(mThumbID, i);
                        if (jsonObject.getString("tags").contains(search)) {
                            mThumbIds.add(mThumbID);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Internet problem", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
