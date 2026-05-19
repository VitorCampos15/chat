package com.vitor.server.service;

import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.LoginRequest;
import com.vitor.server.model.Usuario;
import com.vitor.server.repository.UsuarioRepository;

public class LoginService {

    private static final String ADMIN_LOGIN = "admin";
    private static final String TOKEN_ADMIN = "adm";
    private static final String PREFIXO_TOKEN_USUARIO = "usr_";
    private static final String MSG_LOGIN_401 = "Usuário ou senha inválidos";

    private final UsuarioRepository usuarioRepository;

    public LoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private static boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    private static GenericResponse loginErro401() {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(MSG_LOGIN_401);
        r.setToken(null);
        return r;
    }

    public GenericResponse processarLogin(LoginRequest request) {
        if (request == null || isVazio(request.getUsuario()) || isVazio(request.getSenha())) {
            return loginErro401();
        }

        Usuario cadastrado = usuarioRepository.buscarPorUsuario(request.getUsuario());
        if (cadastrado == null || !request.getSenha().equals(cadastrado.getSenha())) {
            return loginErro401();
        }

        String login = request.getUsuario();
        String token = ADMIN_LOGIN.equals(login)
                ? TOKEN_ADMIN
                : PREFIXO_TOKEN_USUARIO + login;

        usuarioRepository.registrarToken(token, login);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setToken(token);
        return ok;
    }
}
