package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EnviarMensagemRequest {

    @JsonProperty("op")
    private String op;

    @JsonProperty("token")
    private String token;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("para")
    private String para;
}
