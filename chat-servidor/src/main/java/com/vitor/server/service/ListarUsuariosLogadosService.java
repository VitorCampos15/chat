package com.vitor.server.service;

import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.ListarUsuariosLogadosRequest;
import com.vitor.server.model.UsuariosLogadosResponse;
import com.vitor.server.network.ClienteRede;
import com.vitor.server.network.ConexaoManager;
import com.vitor.server.repository.UsuarioRepository;

public class ListarUsuariosLogadosService {

    private static final String MSG_TOKEN_INVALIDO = "Token inválido";

    private final UsuarioRepository usuarioRepository;
    private final ConexaoManager conexaoManager;

    public ListarUsuariosLogadosService(UsuarioRepository usuarioRepository, ConexaoManager conexaoManager) {
        this.usuarioRepository = usuarioRepository;
        this.conexaoManager = conexaoManager;
    }

    private boolean validateToken(String token, ClienteRede clienteRede) {
        if (token == null || token.isBlank() || clienteRede == null) {
            return false;
        }
        return usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta()) != null;
    }

    public Object processarListagem(ListarUsuariosLogadosRequest request, ClienteRede clienteRede) {
        String token = request != null ? request.getToken() : null;
        if (!validateToken(token, clienteRede)) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem(MSG_TOKEN_INVALIDO);
            r.setToken(null);
            return r;
        }
        return conexaoManager.montarRespostaUsuariosLogados();
    }
}
