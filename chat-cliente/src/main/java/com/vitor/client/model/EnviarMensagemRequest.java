package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
