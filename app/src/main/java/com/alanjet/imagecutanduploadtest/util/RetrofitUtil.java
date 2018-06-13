package com.alanjet.imagecutanduploadtest.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by hongjian.chen on 2018/6/13.
 */

public class RetrofitUtil {
    private static RetrofitUtil RETROFITUTIL;
    private Retrofit mRetrofit;

    private RetrofitUtil(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)  //添加baseurl
                .addConverterFactory(ScalarsConverterFactory.create()) //添加返回为字符串的支持
                .addConverterFactory(GsonConverterFactory.create()) //create中可以传入其它json对象，默认Gson
                .build();
    }

    public static RetrofitUtil getInstance(String baseUrl) {

        if (RETROFITUTIL == null) {
            synchronized (RetrofitUtil.class) {
                if (RETROFITUTIL == null) {
                    RETROFITUTIL = new RetrofitUtil(baseUrl);
                }
            }
        }

        return RETROFITUTIL;
    }

    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

}
