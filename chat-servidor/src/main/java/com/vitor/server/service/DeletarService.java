package com.vitor.server.service;

import com.vitor.server.model.DeletarUsuarioRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.repository.UsuarioRepository;

public class DeletarService {

    private final UsuarioRepository usuarioRepository;

    public DeletarService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return usuarioRepository.obterLoginPorToken(token) != null;
    }

    private static GenericResponse resposta401TokenInvalido() {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem("Token inválido");
        r.setToken(null);
        return r;
    }

    public GenericResponse processarExclusao(DeletarUsuarioRequest request) {
        String token = request != null ? request.getToken() : null;
        if (!validateToken(token)) {
            return resposta401TokenInvalido();
        }
        String login = usuarioRepository.obterLoginPorToken(token);
        if (login == null) {
            return resposta401TokenInvalido();
        }
        usuarioRepository.removerToken(token);
        usuarioRepository.removerUsuarioPorLogin(login);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Deletado com sucesso");
        ok.setToken(null);
        return ok;
    }
}
