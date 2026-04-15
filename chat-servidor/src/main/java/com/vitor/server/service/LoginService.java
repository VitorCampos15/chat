package com.vitor.server.service;

import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.LoginRequest;
import com.vitor.server.model.Usuario;
import com.vitor.server.repository.UsuarioRepository;

import java.util.UUID;

public class LoginService {

    private final UsuarioRepository usuarioRepository;

    public LoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private String inputValidation(LoginRequest request) {
        String u = request.getUsuario();
        String s = request.getSenha();
        if (u == null || u.isBlank() || s == null || s.isBlank()) {
            return "Usuário e senha são obrigatórios.";
        }
        return null;
    }

    public GenericResponse processarLogin(LoginRequest request) {
        String erro = inputValidation(request);
        if (erro != null) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem(erro);
            r.setToken("");
            return r;
        }

        Usuario cadastrado = usuarioRepository.buscarPorUsuario(request.getUsuario());
        if (cadastrado == null || !request.getSenha().equals(cadastrado.getSenha())) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem("Usuário ou senha incorretos.");
            r.setToken("");
            return r;
        }

        String token = UUID.randomUUID().toString();
        usuarioRepository.registrarToken(token, request.getUsuario());

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Login realizado com sucesso.");
        ok.setToken(token);
        return ok;
    }
}
