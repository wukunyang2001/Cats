package com.example.cats;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private static GifImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.baseline_pets_white_36);

        }

        gifImageView = findViewById(R.id.gif);

        new GifAsyncTask().execute("https://api.thecatapi.com/v1/images/search?format=src&mime_types=image/gif");

    }

    private static class GifAsyncTask extends AsyncTask<String, Void, GifDrawable>{

        @Override
        protected GifDrawable doInBackground(String... strings) {
            GifDrawable gifDrawable = null;
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(strings[0]).openConnection();
                httpURLConnection.setRequestProperty("x-api-key", "dcea80f2-ef1b-4d60-8252-332b2cc946ce");
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();
                gifDrawable = new GifDrawable(buffer.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return gifDrawable;
        }

        @Override
        protected void onPostExecute(GifDrawable gifDrawable) {
            super.onPostExecute(gifDrawable);
            gifImageView.setImageDrawable(gifDrawable);
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
        switch (item.getItemId()){
            case R.id.action_refresh:
                new GifAsyncTask().execute("https://api.thecatapi.com/v1/images/search?format=src&mime_types=image/gif");
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
