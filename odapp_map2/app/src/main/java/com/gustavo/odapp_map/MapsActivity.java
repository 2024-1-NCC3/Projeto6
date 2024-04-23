package com.gustavo.odapp_map;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int CADASTRO_ONG_REQUEST_CODE = 1;
    private List<Ong> ongsList = new ArrayList<>();
    private Spinner spinnerODSFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Inicializa a lista de ONGs com um exemplo
        ongsList.add(new Ong(-23.5505199, -46.6333094, "ONG Exemplo", "https://www.ongexemplo.org"));

        // Configura o Spinner para filtrar os ODS
        spinnerODSFilter = findViewById(R.id.spinnerODSFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerODSFilter.setAdapter(adapter);
        spinnerODSFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Atualize o mapa de acordo com o filtro selecionado
                updateMapWithFilter(position + 1); // +1 porque os arrays são baseados em zero
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Não faz nada
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Configura o botão para abrir a tela de cadastro da ONG
        Button buttonCadastroONG = findViewById(R.id.buttonCadastroONG);
        buttonCadastroONG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CadastroOngActivity.class);
                startActivityForResult(intent, CADASTRO_ONG_REQUEST_CODE);
            }
        });

        // Configura o botão para escolher o filtro
        Button buttonChooseFilter = findViewById(R.id.buttonChooseFilter);
        buttonChooseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CADASTRO_ONG_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String nome = data.getStringExtra("nome");
            String link = data.getStringExtra("link");

            // Adiciona a nova ONG à lista
            ongsList.add(new Ong(latitude, longitude, nome, link));

            // Atualiza o mapa com o filtro selecionado
            updateMapWithFilter(spinnerODSFilter.getSelectedItemPosition() + 1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adiciona um marcador em Sydney e move a câmera para lá
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    // Método para adicionar um marcador ao mapa com nome e link da ONG
    public void addMarker(LatLng location, String nome, String link) {
        mMap.addMarker(new MarkerOptions().position(location).title(nome).snippet(link));
    }

    // Método para atualizar o mapa de acordo com o filtro selecionado
    public void updateMapWithFilter(int filter) {
        mMap.clear(); // Limpa todos os marcadores do mapa

        for (Ong ong : ongsList) {
            if (ong.getOds() == filter) {
                LatLng location = new LatLng(ong.getLatitude(), ong.getLongitude());
                addMarker(location, ong.getNome(), ong.getLink());
            }
        }

        // Mostra um Toast com o filtro selecionado
        String[] odsArray = getResources().getStringArray(R.array.ods_array);
        String filtroSelecionado = odsArray[filter - 1]; // -1 porque os arrays são baseados em zero
        Toast.makeText(this, "Filtro selecionado: " + filtroSelecionado, Toast.LENGTH_SHORT).show();

        // Atualiza o texto do Spinner para mostrar o filtro selecionado
        spinnerODSFilter.setSelection(filter - 1); // -1 porque os arrays são baseados em zero
    }

    // Método para exibir o AlertDialog com as opções de filtro
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um filtro")
                .setItems(R.array.ods_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateMapWithFilter(which + 1); // +1 porque os arrays são baseados em zero
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
