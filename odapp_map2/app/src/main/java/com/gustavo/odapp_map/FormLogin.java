package com.gustavo.odapp_map;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FormLogin extends AppCompatActivity {

    private Button btEntrar;
    private Button btCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        getSupportActionBar().hide();
        IniciarComponentes();

        btEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir a MapsActivity
                Intent intent = new Intent(FormLogin.this, MapsActivity.class);
                startActivity(intent);
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
    }
}
