package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginRequest {

    @JsonProperty("op")
    private String op;

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("senha")
    private String senha;
}
