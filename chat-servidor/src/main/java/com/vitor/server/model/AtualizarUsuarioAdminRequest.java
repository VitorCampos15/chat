package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AtualizarUsuarioAdminRequest {

    @JsonProperty("op")
    private String op;

    @JsonProperty("token_admin")
    private String tokenAdmin;

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("senha")
    private String senha;
}
