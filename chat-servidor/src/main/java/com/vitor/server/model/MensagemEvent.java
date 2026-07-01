package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemEvent {

    @JsonProperty("op")
    private String op;

    @JsonProperty("remetente")
    private String remetente;

    @JsonProperty("mensagem")
    private String mensagem;
}
