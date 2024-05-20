package com.gustavo.odmap;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class FormCadastro extends AppCompatActivity {

    private EditText editNome;
    private EditText editEmail;
    private EditText editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);
        getSupportActionBar().hide();

        editNome = findViewById(R.id.edit_nome);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);

        Button btCadastrar = findViewById(R.id.bt_cadastrar);
        btCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camposPreenchidos()) {
                    cadastrarUsuario();
                } else {
                    Toast.makeText(FormCadastro.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean camposPreenchidos() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        return !TextUtils.isEmpty(nome) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(senha);
    }

    private void cadastrarUsuario() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        Usuario usuario = new Usuario(nome, email, senha);
        ApiService apiService = RetrofitClient.getApiService();

        Call<ResponseBody> call = apiService.cadastrarUsuario(usuario);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(FormCadastro.this, FormLogin.class);
                    startActivity(intent);
                } else {
                    try {
                        // Log the response for debugging
                        String errorBody = response.errorBody().string();
                        Log.e("CadastroError", "Erro ao cadastrar: " + errorBody);
                        Toast.makeText(FormCadastro.this, "Erro ao cadastrar: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("CadastroError", "Erro de rede", t);
                Toast.makeText(FormCadastro.this, "Erro de rede. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

