package com.vitor.server.service;

import com.vitor.server.model.CadastroRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.Usuario;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.validation.ProtocoloValidacao;

public class CadastroService {

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

        if (!ProtocoloValidacao.isUsuarioConforme(usuario)) {
            return ProtocoloValidacao.MSG_USUARIO_FORMATO;
        }

        if (!ProtocoloValidacao.isSenhaConforme(senha)) {
            return ProtocoloValidacao.MSG_SENHA_FORMATO;
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
            r.setToken(null);
            return r;
        }

        if (usuarioRepository.existeUsuario(request.getUsuario())) {
            GenericResponse r = new GenericResponse();
            r.setResposta("401");
            r.setMensagem("Usuário já cadastrado.");
            r.setToken(null);
            return r;
        }

        usuarioRepository.salvar(new Usuario(request.getNome(), request.getUsuario(), request.getSenha()));

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Cadastrado com sucesso.");
        ok.setToken(null);
        return ok;
    }
}
