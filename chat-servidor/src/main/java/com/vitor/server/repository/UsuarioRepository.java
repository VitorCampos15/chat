package com.vitor.server.repository;

import com.vitor.server.model.Usuario;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioRepository {

    private final Map<String, Usuario> usuariosPorLogin = new ConcurrentHashMap<>();
    /** Chave = token (UUID), valor = login do usuário. */
    private final Map<String, String> tokensAtivos = new ConcurrentHashMap<>();

    public boolean existeUsuario(String usuario) {
        return usuario != null && usuariosPorLogin.containsKey(usuario);
    }

    public Usuario buscarPorUsuario(String login) {
        if (login == null) {
            return null;
        }
        return usuariosPorLogin.get(login);
    }

    public void salvar(Usuario usuario) {
        if (usuario == null || usuario.getUsuario() == null) {
            throw new IllegalArgumentException("Usuário inválido para persistência.");
        }
        usuariosPorLogin.put(usuario.getUsuario(), usuario);
    }

    public void registrarToken(String token, String usuario) {
        if (token == null || usuario == null) {
            throw new IllegalArgumentException("Token e usuário são obrigatórios.");
        }
        tokensAtivos.put(token, usuario);
    }
}
