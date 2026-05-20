package com.vitor.server.service;

import com.vitor.server.model.AtualizarUsuarioRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.Usuario;
import com.vitor.server.network.ClienteRede;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.validation.ProtocoloValidacao;

public class AtualizarService {

    private static final String MSG_TOKEN_INVALIDO = "Token inválido";

    private final UsuarioRepository usuarioRepository;

    public AtualizarService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private boolean validateToken(String token, ClienteRede clienteRede) {
        if (token == null || token.isBlank() || clienteRede == null) {
            return false;
        }
        return usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta()) != null;
    }

    private static boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    private String inputValidation(AtualizarUsuarioRequest request) {
        if (request == null) {
            return "Todos os campos devem estar preenchidos.";
        }
        String nome = request.getNome();
        String senha = request.getSenha();
        if (isVazio(nome) || isVazio(senha)) {
            return "Todos os campos devem estar preenchidos.";
        }
        if (!ProtocoloValidacao.isSenhaConforme(senha)) {
            return ProtocoloValidacao.MSG_SENHA_FORMATO;
        }
        return null;
    }

    private static GenericResponse resposta401(String mensagem) {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(mensagem);
        r.setToken(null);
        return r;
    }

    public GenericResponse processarAtualizacao(AtualizarUsuarioRequest request, ClienteRede clienteRede) {
        String token = request != null ? request.getToken() : null;
        if (!validateToken(token, clienteRede)) {
            return resposta401(MSG_TOKEN_INVALIDO);
        }

        String erro = inputValidation(request);
        if (erro != null) {
            return resposta401(erro);
        }

        String login = usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta());
        Usuario cadastrado = usuarioRepository.buscarPorUsuario(login);
        if (cadastrado == null) {
            return resposta401(MSG_TOKEN_INVALIDO);
        }

        cadastrado.setNome(request.getNome());
        cadastrado.setSenha(request.getSenha());
        usuarioRepository.salvar(cadastrado);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Atualizado com sucesso");
        ok.setToken(null);
        return ok;
    }
}
