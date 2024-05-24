package com.gustavo.odmap;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormLogin extends AppCompatActivity {

    private Button btEntrar;
    private Button btCadastrar;
    private EditText editEmail;
    private EditText editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        getSupportActionBar().hide();
        IniciarComponentes();

        btEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String senha = editSenha.getText().toString().trim();
                if (!email.isEmpty() && !senha.isEmpty()) {
                    loginUsuario(email, senha);
                } else {
                    Toast.makeText(FormLogin.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir o FormCadastro
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });
    }

    private void IniciarComponentes(){
        btEntrar = findViewById(R.id.bt_entrar);
        btCadastrar = findViewById(R.id.bt_cadastro);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
    }

    private void loginUsuario(String email, String senha) {
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest loginRequest = new LoginRequest(email, senha);
        Call<ResponseBody> call = apiService.loginUsuario(loginRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        if (responseBody.contains("Login realizado com sucesso")) {
                            // Abrir a MapsActivity
                            Intent intent = new Intent(FormLogin.this, MapsActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(FormLogin.this, responseBody, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("LoginError", "Erro ao fazer login: " + errorBody);
                        Toast.makeText(FormLogin.this, "Erro ao fazer login: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("LoginError", "Erro de rede", t);
                Toast.makeText(FormLogin.this, "Erro de rede. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}