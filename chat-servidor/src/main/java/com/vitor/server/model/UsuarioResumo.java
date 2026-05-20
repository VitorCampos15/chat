package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumo {

    @JsonProperty("usuario")
    private String usuario;

    @JsonProperty("nome")
    private String nome;
}
