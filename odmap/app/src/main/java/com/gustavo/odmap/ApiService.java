package com.gustavo.odmap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register")
    Call<ResponseBody> cadastrarUsuario(@Body Usuario usuario);

    @POST("login")
    Call<ResponseBody> loginUsuario(@Body LoginRequest loginRequest);

    @POST("register-ong")
    Call<ResponseBody> cadastrarOng(@Body OngRequest ongRequest);
}


