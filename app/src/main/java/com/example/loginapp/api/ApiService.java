package com.example.loginapp.api;

import com.example.loginapp.models.LoginRequest;
import com.example.loginapp.models.LoginResponse;
import com.example.loginapp.models.ImageUploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Multipart;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api.php")
    Call<LoginResponse> login(
        @Query("api") String api,
        @Query("token") String token,
        @Body LoginRequest request
    );

    @Multipart
    @POST("api.php")
    Call<ImageUploadResponse> uploadImage(
        @Query("api") String api,
        @Query("token") String token,
        @Part MultipartBody.Part image
    );
}
