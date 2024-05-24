package com.gustavo.odmap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int CADASTRO_ONG_REQUEST_CODE = 1;
    private List<Ong> ongsList = new ArrayList<>();
    private Spinner spinnerODSFilter;
    private String currentLink; // Variable to store the current link

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
            int ods = data.getIntExtra("ods", 0);

            // Log para depuração
            Log.d("MapsActivity", "Adicionando marcador: " + nome + " Lat: " + latitude + " Lon: " + longitude + " ODS: " + ods);

            // Adiciona um marcador com as coordenadas indicadas
            LatLng location = new LatLng(latitude, longitude);
            addMarker(location, nome, link, imagemUri, descricao, telefone, ods);

            // Move a câmera para a nova localização
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

            try {
                List<Integer> odsList = new ArrayList<>();
                odsList.add(ods); // Adiciona o ODS na lista
                ongsList.add(new Ong(latitude, longitude, nome, link, telefone, descricao, odsList, imagemUri));
            } catch (IllegalArgumentException e) {
                Log.e("MapsActivity", "Erro ao criar objeto Ong: " + e.getMessage());
                // Trate a exceção aqui, se necessário
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Log para verificar inicialização do mapa
        Log.d("MapsActivity", "Mapa está pronto");

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

                // Armazenar o link atual
                currentLink = link;

                // Definir as informações nos elementos do layout
                textViewTitle.setText(title);
                textViewDescription.setText(description);
                textViewLink.setText(link);
                textViewPhone.setText(phone);

                // Exemplo de carregamento de imagem, se você tiver a URI da imagem
                String imageUri = parts[0].substring(8);  // Ajuste o índice aqui para pegar a URI corretamente
                if (!imageUri.isEmpty()) {
                    imageView.setVisibility(View.VISIBLE);
                    Picasso.get().load(imageUri).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE); // Esconde a ImageView se não houver imagem
                }

                return view;
            }

        });

        // Listener para cliques no InfoWindow do marcador
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (currentLink != null && !currentLink.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentLink));
                    startActivity(browserIntent);
                }
            }
        });

        // Chamar o método para carregar os dados das ONGs do servidor
        loadOngsFromServer();
    }

    public void addMarker(LatLng location, String nome, String link, String imagemUri, String descricao, String telefone, int ods) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(nome)
                .snippet("Imagem: " + (imagemUri != null ? imagemUri : "") + "\nDescrição: " + descricao + "\nLink: " + link + "\nTelefone: " + telefone + "\nODS: " + ods)
                .icon(BitmapDescriptorFactory.defaultMarker(getHueForODS(ods)));

        // Log para depuração
        Log.d("MapsActivity", "Adicionando marcador com imagem URI: " + imagemUri);

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
                    // Log para depuração
                    Log.e("MapsActivity", "Falha ao carregar a imagem: " + e.getMessage());
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
            if (filter == 0 || ong.getOds().contains(filter)) {
                LatLng location = new LatLng(ong.getLatitude(), ong.getLongitude());
                addMarker(location, ong.getNome(), ong.getLink(), ong.getImagemUri(), ong.getDescricao(), ong.getTelefone(), filter);
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

    private float getHueForODS(int ods) {
        switch (ods) {
            case 1:
                return 0; // Vermelho
            case 2:
                return 30; // Laranja
            case 3:
                return 60; // Amarelo
            case 4:
                return 90; // Verde claro
            case 5:
                return 120; // Verde
            case 6:
                return 150; // Verde escuro
            case 7:
                return 180; // Ciano
            case 8:
                return 210; // Azul claro
            case 9:
                return 240; // Azul
            case 10:
                return 270; // Roxo claro
            case 11:
                return 300; // Roxo
            case 12:
                return 330; // Rosa claro
            case 13:
                return 0; // Vermelho (ajustado de 360 para 0)
            case 14:
                return 30; // Laranja (repetido, mas pode ser uma cor diferente)
            case 15:
                return 60; // Amarelo (repetido, mas pode ser uma cor diferente)
            case 16:
                return 90; // Verde claro (repetido, mas pode ser uma cor diferente)
            case 17:
                return 120; // Verde (repetido, mas pode ser uma cor diferente)
            default:
                return BitmapDescriptorFactory.HUE_BLUE; // Azul padrão
        }
    }

    private void loadOngsFromServer() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Ong>> call = apiService.getOngs();
        call.enqueue(new Callback<List<Ong>>() {
            @Override
            public void onResponse(Call<List<Ong>> call, Response<List<Ong>> response) {
                if (response.isSuccessful()) {
                    try {
                        List<Ong> ongs = response.body();
                        Log.d("MapsActivity", "ONGs recebidas: " + ongs.toString());
                        ongsList = ongs;
                        for (Ong ong : ongsList) {
                            LatLng location = new LatLng(ong.getLatitude(), ong.getLongitude());
                            // Usando apenas o primeiro ODS para a cor do marcador
                            int primaryOds = ong.getOds().isEmpty() ? 0 : ong.getOds().get(0);
                            addMarker(location, ong.getNome(), ong.getLink(), ong.getImagemUri(), ong.getDescricao(), ong.getTelefone(), primaryOds);
                        }
                    } catch (Exception e) {
                        Log.e("MapsActivity", "Erro ao processar a resposta das ONGs", e);
                    }
                } else {
                    Log.e("MapsActivity", "Erro ao carregar ONGs: " + response.message());
                    Toast.makeText(MapsActivity.this, "Erro ao carregar ONGs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ong>> call, Throwable t) {
                Log.e("MapsActivity", "Erro de rede ao carregar ONGs", t);
                Toast.makeText(MapsActivity.this, "Erro de rede. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}