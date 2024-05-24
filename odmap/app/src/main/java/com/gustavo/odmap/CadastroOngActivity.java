package com.gustavo.odmap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroOngActivity extends AppCompatActivity {

    private EditText editTextNome;
    private EditText editTextLink;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private EditText editTextCnpj;
    private RadioGroup radioGroupODS;
    private EditText editTextDescricao;
    private EditText editTextTelefone;
    private Button buttonImagem;
    private String imagemUri = "";  // Definido como string vazia por padrão

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_ong);

        editTextNome = findViewById(R.id.editTextNome);
        editTextLink = findViewById(R.id.editTextLink);
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        editTextCnpj = findViewById(R.id.editTextCnpj);
        radioGroupODS = findViewById(R.id.radioGroupODS);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        editTextTelefone = findViewById(R.id.editTextTelefone);
        buttonImagem = findViewById(R.id.buttonImagem);

        Button buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarOng();
            }
        });

        editTextCnpj.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                String cnpj = s.toString();

                if (cnpj.isEmpty()) {
                    isUpdating = true;
                    editTextCnpj.setText("");
                    editTextCnpj.setSelection(0);
                    return;
                }

                if (cnpj.length() == 14) {
                    cnpj = cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8)
                            + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12, 14);

                    isUpdating = true;
                    editTextCnpj.setText(cnpj);
                    editTextCnpj.setSelection(cnpj.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarImagem();
            }
        });
    }

    private void salvarOng() {
        String nome = editTextNome.getText().toString();
        String link = editTextLink.getText().toString();
        String latitudeStr = editTextLatitude.getText().toString();
        String longitudeStr = editTextLongitude.getText().toString();
        String cnpj = editTextCnpj.getText().toString().replaceAll("[^0-9]", "");
        String descricao = editTextDescricao.getText().toString();
        String telefone = editTextTelefone.getText().toString();

        if (nome.isEmpty() || link.isEmpty() || cnpj.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty() || descricao.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Todos os campos são obrigatórios, exceto a imagem", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latitudeStr);
            longitude = Double.parseDouble(longitudeStr);
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Formato inválido para latitude ou longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CpfCnpjValidator.isCnpj(cnpj)) {
            Toast.makeText(this, "CNPJ inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        final int selectedId = radioGroupODS.getCheckedRadioButtonId();
        final int finalOds;
        if (selectedId != -1) {
            View radioButton = radioGroupODS.findViewById(selectedId);
            int index = radioGroupODS.indexOfChild(radioButton);
            finalOds = index + 1;
        } else {
            finalOds = 0; // ou qualquer valor padrão que você considere apropriado
        }

        if (descricao.length() > 240) {
            Toast.makeText(this, "A descrição deve ter no máximo 240 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        OngRequest ongRequest = new OngRequest(nome, link, latitude, longitude, cnpj, descricao, telefone, finalOds, imagemUri);
        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseBody> call = apiService.cadastrarOng(ongRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CadastroOngActivity.this, "ONG cadastrada com sucesso!", Toast.LENGTH_SHORT).show();

                    // Envia os dados de volta para a atividade de mapa
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("nome", nome);
                    resultIntent.putExtra("link", link);
                    resultIntent.putExtra("latitude", String.valueOf(latitude));
                    resultIntent.putExtra("longitude", String.valueOf(longitude));
                    resultIntent.putExtra("descricao", descricao);
                    resultIntent.putExtra("telefone", telefone);
                    resultIntent.putExtra("imagemUri", imagemUri);
                    resultIntent.putExtra("ods", finalOds);  // Passando ods como int
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("CadastroOngError", "Erro ao cadastrar: " + errorBody);
                        Toast.makeText(CadastroOngActivity.this, "Erro ao cadastrar: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("CadastroOngError", "Erro de rede", t);
                Toast.makeText(CadastroOngActivity.this, "Erro de rede. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selecionarImagem() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/png");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null && "image/png".equals(getContentResolver().getType(selectedImageUri))) {
                    imagemUri = selectedImageUri.toString();
                    Toast.makeText(this, "Imagem PNG selecionada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Selecione uma imagem PNG", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
