package com.example.cats;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private GifImageView gifImageView;

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

        gifImageView = findViewById(R.id.gif);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getGifStored().observe(this, new Observer<byte[]>() {
            @Override
            public void onChanged(byte[] bytes) {
                try {
                    gifImageView.setImageDrawable(new GifDrawable(bytes));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                mainViewModel.getGif();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_download:
                MainActivityPermissionsDispatcher.saveGifWithPermissionCheck(this);
                return true;
            case R.id.action_share:
                shareGif();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("CheckResult")
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void saveGif(){
        Single
                .create(new SingleOnSubscribe<Boolean>() {

                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                        byte[] gifByte = mainViewModel.getGifStored().getValue();
                        File dir = new File(Environment.getExternalStorageDirectory(), "/cat");
                        if(!dir.exists())dir.mkdir();
                        File file = new File(dir, String.format(Locale.CHINA, "%d.gif", System.currentTimeMillis()));
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        if (gifByte != null) {
                            fileOutputStream.write(gifByte);
                        }
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(file));
                        getApplicationContext().sendBroadcast(intent);
                        fileOutputStream.close();
                        emitter.onSuccess(true);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) {
                        if(success) Toast.makeText(MainActivity.this, "Gif saved!", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("CheckResult")
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void shareGif(){
        Single
                .create(new SingleOnSubscribe<File>() {

                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void subscribe(SingleEmitter<File> emitter) throws Exception {
                        byte[] gifByte = mainViewModel.getGifStored().getValue();
                        File dir = new File(Environment.getExternalStorageDirectory(), "/cat");
                        if(!dir.exists())dir.mkdir();
                        File file = new File(dir, "temp_to_share.gif");
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        if (gifByte != null) {
                            fileOutputStream.write(gifByte);
                        }
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        intent.setType("image/gif");
                        startActivity(Intent.createChooser(intent, getResources().getText(R.string.action_share)));
                        fileOutputStream.close();
                        emitter.onSuccess(file);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe();

    }

}
