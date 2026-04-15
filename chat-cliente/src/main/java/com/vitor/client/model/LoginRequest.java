package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginRequest {

    @JsonProperty("op")
    private String op = "login";

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("senha")
    private String senha;
}
