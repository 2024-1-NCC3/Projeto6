package com.gustavo.odmap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.util.List;

public interface ApiService {
    @POST("register")
    Call<ResponseBody> cadastrarUsuario(@Body Usuario usuario);

    @POST("login")
    Call<ResponseBody> loginUsuario(@Body LoginRequest loginRequest);

    @POST("register-ong")
    Call<ResponseBody> cadastrarOng(@Body OngRequest ongRequest);

    @GET("ongs")  // Adiciona um endpoint para obter a lista de ONGs
    Call<List<Ong>> getOngs();
}
