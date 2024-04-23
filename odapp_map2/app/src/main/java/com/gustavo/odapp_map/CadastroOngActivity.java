package com.gustavo.odapp_map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class CadastroOngActivity extends AppCompatActivity {

    private EditText editTextNome;
    private EditText editTextLink;
    private EditText editTextLatitude;
    private EditText editTextLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_ong);

        // Inicialize os EditTexts
        editTextNome = findViewById(R.id.editTextNome);
        editTextLink = findViewById(R.id.editTextLink);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);

        // Configure o botão de salvar/cadastrar
        Button buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarMarcador();
            }
        });
    }

    private void adicionarMarcador() {
        String nome = editTextNome.getText().toString();
        String link = editTextLink.getText().toString();
        double latitude = Double.parseDouble(editTextLatitude.getText().toString());
        double longitude = Double.parseDouble(editTextLongitude.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("nome", nome);
        intent.putExtra("link", link);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        setResult(RESULT_OK, intent);
        finish();
    }
}
