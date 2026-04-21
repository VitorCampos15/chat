package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeletarUsuarioRequest {

    @JsonProperty("op")
    private String op = "deletarUsuario";

    @JsonProperty("token")
    private String token;
}
