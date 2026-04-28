package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Corpo JSON da consulta: sucesso traz {@code nome}/{@code usuario}; erro traz {@code mensagem}.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsultaUsuarioPayload {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("token")
    private String token;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("usuario")
    private String usuario;
}
