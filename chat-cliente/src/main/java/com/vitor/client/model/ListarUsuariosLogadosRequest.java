package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ListarUsuariosLogadosRequest {

    @JsonProperty("op")
    private String op = "listarUsuariosLogados";

    @JsonProperty("token")
    private String token;
}
