package com.gustavo.odmap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OngDeserializer implements JsonDeserializer<Ong> {

    @Override
    public Ong deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonElement nomeElement = json.getAsJsonObject().get("nome");
        JsonElement linkElement = json.getAsJsonObject().get("link");
        JsonElement latitudeElement = json.getAsJsonObject().get("latitude");
        JsonElement longitudeElement = json.getAsJsonObject().get("longitude");
        JsonElement descricaoElement = json.getAsJsonObject().get("descricao");
        JsonElement telefoneElement = json.getAsJsonObject().get("telefone");
        JsonElement imagemUriElement = json.getAsJsonObject().get("imagemUri");
        JsonElement odsElement = json.getAsJsonObject().get("ods");

        String nome = nomeElement != null && !nomeElement.isJsonNull() ? nomeElement.getAsString() : "";
        String link = linkElement != null && !linkElement.isJsonNull() ? linkElement.getAsString() : "";
        double latitude = latitudeElement != null && !latitudeElement.isJsonNull() ? latitudeElement.getAsDouble() : 0.0;
        double longitude = longitudeElement != null && !longitudeElement.isJsonNull() ? longitudeElement.getAsDouble() : 0.0;
        String descricao = descricaoElement != null && !descricaoElement.isJsonNull() ? descricaoElement.getAsString() : "";
        String telefone = telefoneElement != null && !telefoneElement.isJsonNull() ? telefoneElement.getAsString() : "";
        String imagemUri = imagemUriElement != null && !imagemUriElement.isJsonNull() ? imagemUriElement.getAsString() : "";

        // Handle ODS field
        List<Integer> odsList = new ArrayList<>();
        if (odsElement != null && !odsElement.isJsonNull()) {
            String[] odsStrings = odsElement.getAsString().split(", ");
            for (String odsString : odsStrings) {
                try {
                    int ods = Integer.parseInt(odsString.replaceAll("[^0-9]", ""));
                    odsList.add(ods);
                } catch (NumberFormatException e) {
                    // Handle invalid ODS value
                    e.printStackTrace();
                }
            }
        }

        return new Ong(latitude, longitude, nome, link, telefone, descricao, odsList, imagemUri);
    }
}
