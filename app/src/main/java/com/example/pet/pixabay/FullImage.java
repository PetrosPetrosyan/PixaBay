package com.example.pet.pixabay;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class FullImage extends AppCompatActivity {


    private Button download_button;
    private RelativeLayout layout;
    private int position;
    private String url;
    private ImageView bigimgage;
    private String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        layout = findViewById(R.id.layout);
        download_button = findViewById(R.id.download_button);
        download_button.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("position");

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get("https://pixabay.com/api/?key=10854982-e556" +
                "022b1b07997f06e504502&q=yellow+flowers&image_type=photo&pretty=true", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray jsonArray = response.getJSONArray("hits");
                    JSONObject jsonObject = jsonArray.getJSONObject(position);
                    url = jsonObject.getString("webformatURL");
                    bigimgage = new ImageView(FullImage.this);
                    loadImage(url, bigimgage);
                    LinearLayout.LayoutParams params = new LinearLayout
                            .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 40, 0, 30);
                    bigimgage.setLayoutParams(params);
                    layout.addView(bigimgage);
                    download_button.setVisibility(View.VISIBLE);
                    filename=jsonObject.getString("tags").replaceAll(", ","_")+".jpg";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        download_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                DownloadImage downloadTask = new DownloadImage();
                downloadTask.execute(url);
            }
        });

    }

    private void loadImage(String url, ImageView imageView) {
        Glide
                .with(imageView.getContext())
                .load(url)
                .thumbnail(Glide.with(getApplicationContext()).load(R.drawable.loader))
                .into(imageView);
    }

    private class DownloadImage extends AsyncTask<String, Integer, String> {

        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(FullImage.this);
            mProgressDialog.setTitle("Download Image Tutorial");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... stringURL) {
            String imageURL = stringURL[0];
            int file_length = 0;
            try {
                URL urlURL = new URL(imageURL);
                URLConnection urlConnection = urlURL.openConnection();
                urlConnection.connect();
                file_length = urlConnection.getContentLength();

                File new_folder = new File(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath() + "/DCIM/Camera/");

                if (!new_folder.exists()) {
                    new_folder.mkdir();
                }

                File output_file = new File(new_folder, filename);

                OutputStream outputStream = new FileOutputStream(output_file);
                InputStream inputStream = new BufferedInputStream(urlURL.openStream(), 8192);

                byte[] data = new byte[1024];
                int total = 0;
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    outputStream.write(data, 0, count);
                    int progress = 100 * total / file_length;
                    publishProgress(progress);
                }
                inputStream.close();
                outputStream.close();

                addImageToGallery(new_folder.toString(), getApplicationContext(),filename);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Url Error";
            } catch (IOException e) {
                e.printStackTrace();
                return "File Directory error";
            }
            return "Download Complate";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.hide();
//            File folder = new File(Environment
//                    .getExternalStorageDirectory()
//                    .getAbsolutePath() + "/DCIM/Camera/");
//            File output_file = new File(folder, "downloaded_image.jpg");
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
//            String path = output_file.toString();
//            bigimgage.setImageDrawable(Drawable.createFromPath(path));
//            Log.i("Info", "Path: " + path);
        }

        public void addImageToGallery(final String filePath, final Context context,final String file_name) {

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, file_name);
            values.put(MediaStore.MediaColumns.DATA, filePath);

            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

}
