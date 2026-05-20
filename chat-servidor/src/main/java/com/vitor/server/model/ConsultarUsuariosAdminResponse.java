package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsultarUsuariosAdminResponse {

    @JsonProperty("resposta")
    private String resposta;

    @JsonProperty("lista_usuarios")
    private List<UsuarioResumo> listaUsuarios;
}
