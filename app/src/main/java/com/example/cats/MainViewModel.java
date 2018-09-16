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

public class MainViewModel extends ViewModel {

    private MutableLiveData<byte[]> gifStored;

    private MutableLiveData<Boolean> isLoading;

    public LiveData<byte[]> getGifStored() {
        if(gifStored == null){
            gifStored = new MutableLiveData<>();
            getGif();
        }
        return gifStored;
    }

    @SuppressLint("CheckResult")
    public void getGif(){
        isLoading.setValue(true);
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
                .map(new Function<InputStream, byte[]>() {
                    @Override
                    public byte[] apply(InputStream inputStream) throws Exception {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }

                        buffer.flush();
                        return buffer.toByteArray();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) {
                        gifStored.setValue(bytes);
                        isLoading.setValue(false);
                    }
                });
    }

    public LiveData<Boolean> getIsLoading(){
        if(isLoading == null){
            isLoading = new MutableLiveData<>();
            isLoading.setValue(true);
        }
        return isLoading;
    }

}
