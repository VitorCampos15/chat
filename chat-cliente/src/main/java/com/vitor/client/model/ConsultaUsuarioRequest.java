package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConsultaUsuarioRequest {

    @JsonProperty("op")
    private String op = "consultaUsuario";

    @JsonProperty("token")
    private String token;
}
