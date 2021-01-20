package com.ilkom.dagingwarning.app;

import com.ilkom.dagingwarning.ServerResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiConfig {
    static String BASE_URL="http://10.236.213.15:80/daging/";
    @Multipart
    @POST("/upload")
    Call<ServerResponse> uploadImage(@Part MultipartBody.Part image);

}