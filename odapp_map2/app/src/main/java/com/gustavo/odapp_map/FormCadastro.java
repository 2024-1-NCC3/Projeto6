package com.gustavo.odapp_map;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
                    // Se os campos estiverem preenchidos, continuar com o cadastro
                    cadastrarUsuario();
                } else {
                    // Se algum campo não estiver preenchido, exibir mensagem de erro
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
        // Adicione aqui a lógica para cadastrar o usuário
        // Por exemplo, você pode usar um serviço ou API para fazer isso
        // Neste exemplo, vamos apenas voltar para a tela de login
        Intent intent = new Intent(FormCadastro.this, FormLogin.class);
        startActivity(intent);
    }
}

