package com.vitor.server.repository;

import com.vitor.server.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioRepository {

    private static final String ADMIN_LOGIN = "admin";
    private static final String TOKEN_ADMIN = "adm";
    private static final String SEPARADOR_CHAVE = "#";

    /** Chave = token#ip#porta, valor = login do usuário. */
    private final Map<String, String> tokensAtivos = new ConcurrentHashMap<>();
    private final Map<String, Usuario> usuariosPorLogin = new ConcurrentHashMap<>();

    public UsuarioRepository() {
        inicializarAdmin();
    }

    private void inicializarAdmin() {
        if (!existeUsuario(ADMIN_LOGIN)) {
            salvar(new Usuario("admin", ADMIN_LOGIN, "123456"));
        }
    }

    public static String montarChaveToken(String token, String ip, int porta) {
        return token + SEPARADOR_CHAVE + ip + SEPARADOR_CHAVE + porta;
    }

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

    /**
     * Registra sessão ativa vinculada ao IP/porta do socket que realizou o login.
     * Token {@code adm} é registrado com chave composta, mas validado sem restrição de rede.
     */
    public void registrarToken(String token, String usuario, String ip, int porta) {
        if (token == null || usuario == null || ip == null) {
            throw new IllegalArgumentException("Token, usuário e IP são obrigatórios.");
        }
        if (!TOKEN_ADMIN.equals(token)) {
            removerTokensPorLogin(usuario);
        }
        tokensAtivos.put(montarChaveToken(token, ip, porta), usuario);
    }

    /**
     * Remove o token da sessão atual (mesmo IP/porta da conexão).
     */
    public boolean removerToken(String token, String ip, int porta) {
        if (token == null || ip == null) {
            return false;
        }
        if (TOKEN_ADMIN.equals(token)) {
            String chave = montarChaveToken(token, ip, porta);
            if (tokensAtivos.remove(chave) != null) {
                return true;
            }
            return existeUsuario(ADMIN_LOGIN);
        }
        return tokensAtivos.remove(montarChaveToken(token, ip, porta)) != null;
    }

    /**
     * Resolve o login se o token existir para o par IP/porta da conexão atual.
     * Token {@code adm} aceita qualquer origem de rede (exceção para testes de admin).
     */
    public String obterLoginPorToken(String token, String ip, int porta) {
        if (token == null || token.isBlank() || ip == null) {
            return null;
        }
        if (TOKEN_ADMIN.equals(token)) {
            return existeUsuario(ADMIN_LOGIN) ? ADMIN_LOGIN : null;
        }
        return tokensAtivos.get(montarChaveToken(token, ip, porta));
    }

    /** Remove o cadastro do usuário identificado pelo login. */
    public void removerUsuarioPorLogin(String login) {
        if (login != null) {
            usuariosPorLogin.remove(login);
        }
    }

    /** Lista todos os usuários cadastrados (cópia defensiva). */
    public List<Usuario> listarTodosUsuarios() {
        return new ArrayList<>(usuariosPorLogin.values());
    }

    /** Remove todos os tokens ativos associados ao login informado (logout forçado). */
    public void removerTokensPorLogin(String login) {
        if (login == null) {
            return;
        }
        tokensAtivos.entrySet().removeIf(entry -> login.equals(entry.getValue()));
    }
}
