package com.example.cats;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.example.cats.database.Cat;
import com.example.cats.database.CatDao;
import com.example.cats.database.CatDatabase;

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

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> isLoading;
    private CatDao catDao;
    private LiveData<Cat> catLiveData;

    public MainViewModel(@NonNull Application application) {
        super(application);
        catDao = CatDatabase.getDatabase(application).catDao();
        catLiveData = catDao.query();
    }

    public LiveData<Cat> getCatLiveData() {
        return catLiveData;
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
                .map(new Function<InputStream, Boolean>() {
                    @Override
                    public Boolean apply(InputStream inputStream) throws Exception {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }

                        buffer.flush();

                        catDao.delete();
                        catDao.insert(new Cat(buffer.toByteArray()));

                        buffer.close();
                        inputStream.close();

                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) {
                        isLoading.setValue(false);
                    }
                });
    }

    public LiveData<Boolean> getIsLoading(){
        if(isLoading == null){
            isLoading = new MutableLiveData<>();
            isLoading.setValue(false);
        }
        return isLoading;
    }

}
