package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenericResponse {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("token")
    private String token;
}
