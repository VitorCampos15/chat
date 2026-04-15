package com.vitor.server.service;

import com.vitor.server.model.CadastroRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.Usuario;
import com.vitor.server.repository.UsuarioRepository;

import java.util.regex.Pattern;

public class CadastroService {

    private static final Pattern SENHA_SEIS_DIGITOS = Pattern.compile("^\\d{6}$");

    private final UsuarioRepository usuarioRepository;

    public CadastroService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private String inputValidation(CadastroRequest request) {
        String nome = request.getNome();
        String usuario = request.getUsuario();
        String senha = request.getSenha();

        if (isVazio(nome) || isVazio(usuario) || isVazio(senha)) {
            return "Todos os campos devem estar preenchidos.";
        }

        int uLen = usuario.length();
        if (uLen < 5 || uLen > 20 || usuario.contains(" ")) {
            return "Usuário inválido (deve ter entre 5 e 20 caracteres e sem espaços).";
        }

        if (!SENHA_SEIS_DIGITOS.matcher(senha).matches()) {
            return "Senha inválida. Use apenas números e exatamente 6 dígitos.";
        }

        return null;
    }

    private static boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    public GenericResponse processarCadastro(CadastroRequest request) {
        String erro = inputValidation(request);
        if (erro != null) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem(erro);
            r.setToken("");
            return r;
        }

        if (usuarioRepository.existeUsuario(request.getUsuario())) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem("Usuário já cadastrado.");
            r.setToken("");
            return r;
        }

        usuarioRepository.salvar(new Usuario(request.getNome(), request.getUsuario(), request.getSenha()));

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Cadastrado com sucesso.");
        ok.setToken("");
        return ok;
    }
}
