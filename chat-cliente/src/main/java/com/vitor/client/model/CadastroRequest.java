package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CadastroRequest {

    @JsonProperty("op")
    private String op = "cadastrarUsuario";

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("senha")
    private String senha;
}
