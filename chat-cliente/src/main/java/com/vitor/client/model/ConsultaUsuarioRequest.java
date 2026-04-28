package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConsultaUsuarioRequest {

    @JsonProperty("op")
    private String op = "consultarUsuario";

    @JsonProperty("token")
    private String token;
}
