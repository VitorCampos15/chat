package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminRequest {

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
