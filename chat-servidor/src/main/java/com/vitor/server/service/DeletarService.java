package com.vitor.server.service;

import com.vitor.server.model.DeletarUsuarioRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.network.ClienteRede;
import com.vitor.server.repository.UsuarioRepository;

public class DeletarService {

    private static final String MSG_TOKEN_INVALIDO = "Token inválido";

    private final UsuarioRepository usuarioRepository;

    public DeletarService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private boolean validateToken(String token, ClienteRede clienteRede) {
        if (token == null || token.isBlank() || clienteRede == null) {
            return false;
        }
        return usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta()) != null;
    }

    private static GenericResponse resposta401TokenInvalido() {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(MSG_TOKEN_INVALIDO);
        r.setToken(null);
        return r;
    }

    public GenericResponse processarExclusao(DeletarUsuarioRequest request, ClienteRede clienteRede) {
        String token = request != null ? request.getToken() : null;
        if (!validateToken(token, clienteRede)) {
            return resposta401TokenInvalido();
        }
        String login = usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta());
        if (login == null) {
            return resposta401TokenInvalido();
        }
        usuarioRepository.removerToken(token, clienteRede.ip(), clienteRede.porta());
        usuarioRepository.removerUsuarioPorLogin(login);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Deletado com sucesso");
        ok.setToken(null);
        return ok;
    }
}
