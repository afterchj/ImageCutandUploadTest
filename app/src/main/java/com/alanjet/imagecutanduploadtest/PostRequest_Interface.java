package com.alanjet.imagecutanduploadtest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by hongjian.chen on 2018/6/12.
 */

public interface PostRequest_Interface {
    @POST("upload")
    @Multipart
    Call<Translation> upload(@Part("uid") RequestBody uid, @Part MultipartBody.Part file);

    @Streaming //大文件时要加不然会OOM
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

}
