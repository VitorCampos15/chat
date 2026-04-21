package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ConsultaUsuarioResponse {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("usuario")
    private String usuario;
}
