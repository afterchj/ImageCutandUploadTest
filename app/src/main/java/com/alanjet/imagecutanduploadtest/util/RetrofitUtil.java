package com.alanjet.imagecutanduploadtest.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by hongjian.chen on 2018/6/13.
 */

public class RetrofitUtil {

    private Retrofit mRetrofit;

    public RetrofitUtil(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)  //添加baseurl
                .addConverterFactory(ScalarsConverterFactory.create()) //添加返回为字符串的支持
                .addConverterFactory(GsonConverterFactory.create()) //create中可以传入其它json对象，默认Gson
                .build();
    }


    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

}
