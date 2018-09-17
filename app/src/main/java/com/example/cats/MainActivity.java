package com.example.cats;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cats.database.Cat;

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
    private ProgressBar progressBar;
    private ClipboardManager clipboardManager;

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
        progressBar = findViewById(R.id.progressBar);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean visible) {
                if(visible) progressBar.setVisibility(ProgressBar.VISIBLE);
                else progressBar.setVisibility(ProgressBar.GONE);
            }
        });
        mainViewModel.getCatLiveData().observe(this, new Observer<Cat>() {
            @Override
            public void onChanged(@Nullable Cat cat) {
                try {
                    if(cat != null) gifImageView.setImageDrawable(new GifDrawable(cat.catGif));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        registerForContextMenu(gifImageView);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

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
                MainActivityPermissionsDispatcher.shareGifWithPermissionCheck(this);
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
                        Cat cat = mainViewModel.getCatLiveData().getValue();
                        if(cat == null) return;
                        byte[] gifByte = cat.catGif;
                        File file = new File(Environment.getExternalStorageDirectory(), "/cat");
                        if(!file.exists())file.mkdir();
                        file = new File(file, String.format(Locale.CHINA, "%d.gif", System.currentTimeMillis()));
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(gifByte);
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
                .create(new SingleOnSubscribe<Uri>() {

                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void subscribe(SingleEmitter<Uri> emitter) throws Exception {
                        Cat cat = mainViewModel.getCatLiveData().getValue();
                        if(cat == null) return;
                        byte[] gifByte = cat.catGif;
                        File file = new File(Environment.getExternalStorageDirectory(), "/cat/share");
                        if(!file.exists()) file.mkdirs();
                        file = new File(file, "temp_to_share.gif");
                        if(file.exists()) file.delete();
                        file.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(gifByte);
                        fileOutputStream.close();
                        emitter.onSuccess(Uri.fromFile(file));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(Uri uri) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.setType("image/gif");
                        startActivity(Intent.createChooser(intent, getResources().getText(R.string.action_share)));
                    }
                });
    }

    @SuppressLint("CheckResult")
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void copyGif(){
        Single
                .create(new SingleOnSubscribe<File>() {

                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void subscribe(SingleEmitter<File> emitter) throws Exception {
                        Cat cat = mainViewModel.getCatLiveData().getValue();
                        if(cat == null) return;
                        byte[] gifByte = cat.catGif;
                        File file = File.createTempFile("temp_to_copy", "gif");
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(gifByte);
                        fileOutputStream.close();
                        emitter.onSuccess(file);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        ClipData clipData = ClipData.newRawUri("URI", Uri.fromFile(file));
                        clipboardManager.setPrimaryClip(clipData);
                    }
                });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.context_refresh:
                mainViewModel.getGif();
                return true;
            case R.id.context_save:
                MainActivityPermissionsDispatcher.saveGifWithPermissionCheck(this);
                return true;
            case R.id.context_share:
                MainActivityPermissionsDispatcher.shareGifWithPermissionCheck(this);
                return true;
            case R.id.context_copy:
                MainActivityPermissionsDispatcher.copyGifWithPermissionCheck(this);
                return true;
        }
        return super.onContextItemSelected(item);
    }

}
