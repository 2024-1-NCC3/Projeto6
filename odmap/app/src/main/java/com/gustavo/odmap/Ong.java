package com.gustavo.odmap;

public class Ong {
    private final double latitude;
    private final double longitude;
    private final String nome;
    private final String link;
    private final String telefone;
    private final String descricao;
    private final String ods;
    private final String imagemUri; // Campo para armazenar a URI da imagem

    public Ong(double latitude, double longitude, String nome, String link, String telefone, String descricao, String ods, String imagemUri) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenadas de latitude e longitude inválidas");
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.nome = nome;
        this.link = link;
        this.telefone = telefone;
        this.descricao = descricao;
        this.ods = ods;
        this.imagemUri = imagemUri;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getNome() {
        return nome;
    }

    public String getLink() {
        return link;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getOds() {
        return ods;
    }

    public String getImagemUri() {
        return imagemUri;
    }

    @Override
    public String toString() {
        return "Ong{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", nome='" + nome + '\'' +
                ", link='" + link + '\'' +
                ", telefone='" + telefone + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ods='" + ods + '\'' +
                ", imagemUri='" + (imagemUri != null ? imagemUri : "N/A") + '\'' +
                '}';
    }
}
