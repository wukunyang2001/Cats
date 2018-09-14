package com.example.cats;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private String url = "https://api.thecatapi.com/v1/images/search?format=src&mime_types=image/gif";

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

        getGif(url);

    }

    @SuppressLint("CheckResult")
    private void getGif(final String url){
        io.reactivex.Observable
                .create(new ObservableOnSubscribe<InputStream>() {

                    @Override
                    public void subscribe(ObservableEmitter<InputStream> emitter) throws Exception {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                        httpURLConnection.setRequestProperty("x-api-key", "dcea80f2-ef1b-4d60-8252-332b2cc946ce");
                        httpURLConnection.connect();
                        emitter.onNext(httpURLConnection.getInputStream());
                    }
                })
                .map(new Function<InputStream, GifDrawable>() {
                    @Override
                    public GifDrawable apply(InputStream inputStream) throws Exception {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }

                        buffer.flush();
                        return new GifDrawable(buffer.toByteArray());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<GifDrawable>() {
                    @Override
                    public void accept(GifDrawable gifDrawable) {
                        GifImageView gifImageView = findViewById(R.id.gif);
                        gifImageView.setImageDrawable(gifDrawable);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                getGif(url);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
