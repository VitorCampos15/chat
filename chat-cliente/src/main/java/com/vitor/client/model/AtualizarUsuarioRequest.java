package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AtualizarUsuarioRequest {

    @JsonProperty("op")
    private String op = "atualizarUsuario";

    @JsonProperty("token")
    private String token;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("senha")
    private String senha;
}
