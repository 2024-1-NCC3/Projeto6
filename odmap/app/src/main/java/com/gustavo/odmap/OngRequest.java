package com.gustavo.odmap;

public class OngRequest {
    public String nome;
    public String link;
    public double latitude;
    public double longitude;
    public String cnpj;
    public String descricao;
    public String telefone;
    public int ods;  // ods deve ser int, não String
    public String imagemUri;

    public OngRequest(String nome, String link, double latitude, double longitude, String cnpj, String descricao, String telefone, int ods, String imagemUri) {
        this.nome = nome;
        this.link = link;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cnpj = cnpj;
        this.descricao = descricao;
        this.telefone = telefone;
        this.ods = ods;
        this.imagemUri = imagemUri;
    }
}
