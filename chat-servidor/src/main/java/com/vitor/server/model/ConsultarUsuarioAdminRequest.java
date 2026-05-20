package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConsultarUsuarioAdminRequest {

    @JsonProperty("op")
    private String op;

    @JsonProperty("token_admin")
    private String tokenAdmin;

    @JsonProperty("usuario")
    private String usuario;
}
