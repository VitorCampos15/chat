package com.vitor.server.service;

import com.vitor.server.model.ConsultaUsuarioRequest;
import com.vitor.server.model.ConsultaUsuarioResponse;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.Usuario;
import com.vitor.server.repository.UsuarioRepository;

public class ConsultaService {

    private final UsuarioRepository usuarioRepository;

    public ConsultaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return usuarioRepository.obterLoginPorToken(token) != null;
    }

    /**
     * Sucesso: {@link ConsultaUsuarioResponse} com resposta, nome e usuario.
     * Falha: {@link GenericResponse} com 401 e mensagem.
     */
    public Object processarConsulta(ConsultaUsuarioRequest request) {
        String token = request != null ? request.getToken() : null;
        if (!validateToken(token)) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem("Token inválido");
            r.setToken(null);
            return r;
        }
        String login = usuarioRepository.obterLoginPorToken(token);
        Usuario cadastrado = usuarioRepository.buscarPorUsuario(login);
        if (cadastrado == null) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem("Token inválido");
            r.setToken(null);
            return r;
        }
        ConsultaUsuarioResponse ok = new ConsultaUsuarioResponse();
        ok.setResposta("200");
        ok.setNome(cadastrado.getNome());
        ok.setUsuario(cadastrado.getUsuario());
        return ok;
    }
}
