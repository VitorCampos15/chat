package com.vitor.server.service;

import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.LogoutRequest;
import com.vitor.server.network.ClienteRede;
import com.vitor.server.repository.UsuarioRepository;

public class LogoutService {

    private final UsuarioRepository usuarioRepository;

    public LogoutService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private String inputValidation(LogoutRequest request) {
        if (request == null) {
            return "Token obrigatório.";
        }
        String t = request.getToken();
        if (t == null || t.isBlank()) {
            return "Token obrigatório.";
        }
        return null;
    }

    private static GenericResponse respostaLogoutFalhou() {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem("Erro ao efetuar logout");
        r.setToken(null);
        return r;
    }

    public GenericResponse processarLogout(LogoutRequest request, ClienteRede clienteRede) {
        if (inputValidation(request) != null || clienteRede == null) {
            return respostaLogoutFalhou();
        }
        String token = request.getToken();
        if (usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta()) == null) {
            return respostaLogoutFalhou();
        }
        if (!usuarioRepository.removerToken(token, clienteRede.ip(), clienteRede.porta())) {
            return respostaLogoutFalhou();
        }
        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("logout efetuado");
        ok.setToken(null);
        return ok;
    }
}
