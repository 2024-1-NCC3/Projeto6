package com.gustavo.odmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

        setupODSSpinner();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button buttonCadastroONG = findViewById(R.id.buttonCadastroONG);
        buttonCadastroONG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, CadastroOngActivity.class);
                startActivityForResult(intent, CADASTRO_ONG_REQUEST_CODE);
            }
        });

        Button buttonChooseFilter = findViewById(R.id.buttonChooseFilter);
        buttonChooseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    private void setupODSSpinner() {
        spinnerODSFilter = findViewById(R.id.spinnerODSFilter);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerODSFilter.setAdapter(adapter);
        spinnerODSFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateMapWithFilter(position + 1); // +1 porque os arrays são baseados em zero
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Não faz nada
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRO_ONG_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String nome = data.getStringExtra("nome");
            String link = data.getStringExtra("link");
            double latitude = Double.parseDouble(data.getStringExtra("latitude"));
            double longitude = Double.parseDouble(data.getStringExtra("longitude"));
            String descricao = data.getStringExtra("descricao");
            String telefone = data.getStringExtra("telefone");
            String imagemUri = data.getStringExtra("imagemUri");
            String ods = data.getStringExtra("ods");

            // Adiciona um marcador com as coordenadas indicadas
            LatLng location = new LatLng(latitude, longitude);
            addMarker(location, nome, link, imagemUri, descricao, telefone, ods);

            // Move a câmera para a nova localização
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

            try {
                ongsList.add(new Ong(latitude, longitude, nome, link, telefone, descricao, ods, imagemUri));
            } catch (IllegalArgumentException e) {
                Log.e("MapsActivity", "Erro ao criar objeto Ong: " + e.getMessage());
                // Trate a exceção aqui, se necessário
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                TextView textViewTitle = view.findViewById(R.id.textViewTitle);
                ImageView imageView = view.findViewById(R.id.imageView);
                TextView textViewDescription = view.findViewById(R.id.textViewDescription);
                TextView textViewLink = view.findViewById(R.id.textViewLink);
                TextView textViewPhone = view.findViewById(R.id.textViewPhone);

                // Obter as informações da ONG do marcador
                String title = marker.getTitle();
                String snippet = marker.getSnippet();

                // Separar as informações do snippet
                String[] parts = snippet.split("\n");
                String description = parts[1].substring(12);
                String link = parts[2].substring(6);
                String phone = parts[3].substring(10);

                // Definir as informações nos elementos do layout
                textViewTitle.setText(title);
                textViewDescription.setText(description);
                textViewLink.setText(link);
                textViewPhone.setText(phone);

                // Exemplo de carregamento de imagem, se você tiver a URI da imagem
                String imageUri = parts[0].substring(8);  // Ajuste o índice aqui para pegar a URI corretamente
                if (!imageUri.isEmpty()) {
                    Picasso.get().load(imageUri).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE); // Esconde a ImageView se não houver imagem
                }

                return view;
            }

        });

        updateMapWithFilter(0);
    }

    public void addMarker(LatLng location, String nome, String link, String imagemUri, String descricao, String telefone, String ods) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(nome)
                .snippet("Imagem: " + (imagemUri != null ? imagemUri : "") + "\nDescrição: " + descricao + "\nLink: " + link + "\nTelefone: " + telefone + "\nODS: " + ods)
                .icon(BitmapDescriptorFactory.defaultMarker(getHueForODS(ods)));

        // Define a imagem personalizada do marcador, se imagemUri não for nula
        if (imagemUri != null && !imagemUri.isEmpty()) {
            Picasso.get().load(imagemUri).resize(100, 100).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                    markerOptions.icon(bitmapDescriptor);
                    mMap.addMarker(markerOptions);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    // Trate a falha ao carregar a imagem aqui, se necessário
                    mMap.addMarker(markerOptions); // Adicione o marcador mesmo se a imagem falhar ao carregar
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // Este método pode ser deixado vazio
                }
            });
        } else {
            mMap.addMarker(markerOptions);
        }
    }

    public void updateMapWithFilter(int filter) {
        mMap.clear();

        for (Ong ong : ongsList) {
            if (filter == 0 || ong.getOds().equals(String.valueOf(filter))) {
                LatLng location = new LatLng(ong.getLatitude(), ong.getLongitude());
                addMarker(location, ong.getNome(), ong.getLink(), ong.getImagemUri(), ong.getDescricao(), ong.getTelefone(), ong.getOds());
            }
        }

        String[] odsArray = getResources().getStringArray(R.array.ods_array);
        if (filter >= 1 && filter <= odsArray.length) {
            String filtroSelecionado = odsArray[filter - 1];
            Toast.makeText(this, "Filtro selecionado: " + filtroSelecionado, Toast.LENGTH_SHORT).show();
            spinnerODSFilter.setSelection(filter - 1);
        } else {
            //não faz nada
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um filtro")
                .setItems(R.array.ods_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateMapWithFilter(which + 1);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private float getHueForODS(String ods) {
        switch (ods) {
            case "ODS 1":
                return 0; // Vermelho
            case "ODS 2":
                return 30; // Laranja
            case "ODS 3":
                return 60; // Amarelo
            case "ODS 4":
                return 90; // Verde claro
            case "ODS 5":
                return 120; // Verde
            case "ODS 6":
                return 150; // Verde escuro
            case "ODS 7":
                return 180; // Ciano
            case "ODS 8":
                return 210; // Azul claro
            case "ODS 9":
                return 240; // Azul
            case "ODS 10":
                return 270; // Roxo claro
            case "ODS 11":
                return 300; // Roxo
            case "ODS 12":
                return 330; // Rosa claro
            case "ODS 13":
                return 360; // Vermelho (ou 0)
            case "ODS 14":
                return 30; // Laranja (repetido, mas pode ser uma cor diferente)
            case "ODS 15":
                return 60; // Amarelo (repetido, mas pode ser uma cor diferente)
            case "ODS 16":
                return 90; // Verde claro (repetido, mas pode ser uma cor diferente)
            case "ODS 17":
                return 120; // Verde (repetido, mas pode ser uma cor diferente)
            default:
                return BitmapDescriptorFactory.HUE_BLUE; // Azul padrão
        }
    }
}
