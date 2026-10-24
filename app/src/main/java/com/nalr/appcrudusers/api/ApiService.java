package com.nalr.appcrudusers.api;

import com.nalr.appcrudusers.model.CreateUser;
import com.nalr.appcrudusers.model.LoginRequest;
import com.nalr.appcrudusers.model.LoginResponse;
import com.nalr.appcrudusers.model.UpdateUser;
import com.nalr.appcrudusers.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/users")
    Call<List<User>> users(@Header("Authorization") String token);

    @GET("api/users/{id}")
    Call<User> user(@Header("Authorization") String token, @Path("id") String id);

    @POST("api/users")
    Call<User> create(@Body CreateUser request);

    @POST("api/users")
    Call<User> createAuthorized(@Header("Authorization") String token, @Body CreateUser request);

    @PUT("api/users/{id}")
    Call<User> update(@Header("Authorization") String token, @Path("id") String id, @Body UpdateUser request);

    @DELETE("api/users/{id}")
    Call<Void> delete(@Header("Authorization") String token, @Path("id") String id);
}
