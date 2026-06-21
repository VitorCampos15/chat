package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuariosLogadosResponse {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("usuarios_logados")
    private List<String> usuariosLogados;
}
