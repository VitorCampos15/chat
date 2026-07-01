package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("op")
    private String op;

    @JsonProperty("remetente")
    private String remetente;

    @JsonProperty("mensagem")
    private String mensagem;

    /** Formato legado de servidores antigos. */
    @JsonProperty("de")
    private String de;

    @JsonProperty("destinatario")
    private String destinatario;
}
