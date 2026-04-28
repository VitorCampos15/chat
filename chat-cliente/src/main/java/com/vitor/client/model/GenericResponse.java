package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("token")
    private String token;
}
