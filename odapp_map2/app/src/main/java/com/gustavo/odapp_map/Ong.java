package com.gustavo.odapp_map;

public class Ong {
    private double latitude;
    private double longitude;
    private String nome;
    private String link;

    public Ong(double latitude, double longitude, String nome, String link) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.nome = nome;
        this.link = link;
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

    public int getOds() {
        // Aqui você pode implementar a lógica para determinar o ODS da ONG
        // com base em suas características, se necessário
        return 0; // Por padrão, retorna 0
    }
}
