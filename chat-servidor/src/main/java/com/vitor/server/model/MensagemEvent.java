package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemEvent {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("de")
    private String de;

    @JsonProperty("destinatario")
    private String destinatario;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("mensagem_erro")
    private String mensagemErro;
}
