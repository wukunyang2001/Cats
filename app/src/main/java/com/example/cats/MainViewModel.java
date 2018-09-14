package com.example.cats;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;

public class MainViewModel extends ViewModel {

    private MutableLiveData<GifDrawable> gifStored;

    public LiveData<GifDrawable> getGifStored() {
        if(gifStored == null){
            gifStored = new MutableLiveData<>();
            getGif();
        }
        return gifStored;
    }

    @SuppressLint("CheckResult")
    public void getGif(){
        Single
                .create(new SingleOnSubscribe<InputStream>(){

                    @Override
                    public void subscribe(SingleEmitter<InputStream> emitter) throws Exception {
                        String url = "https://api.thecatapi.com/v1/images/search?format=src&mime_types=image/gif";
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                        httpURLConnection.setRequestProperty("x-api-key", "dcea80f2-ef1b-4d60-8252-332b2cc946ce");
                        httpURLConnection.connect();
                        emitter.onSuccess(httpURLConnection.getInputStream());
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
                        gifStored.setValue(gifDrawable);
                    }
                });
    }


}
