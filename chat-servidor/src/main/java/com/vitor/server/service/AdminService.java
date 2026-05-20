package com.vitor.server.service;

import com.vitor.server.model.AtualizarUsuarioAdminRequest;
import com.vitor.server.model.ConsultarUsuarioAdminRequest;
import com.vitor.server.model.ConsultarUsuariosAdminRequest;
import com.vitor.server.model.ConsultarUsuariosAdminResponse;
import com.vitor.server.model.ConsultaUsuarioResponse;
import com.vitor.server.model.DeletarUsuarioAdminRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.Usuario;
import com.vitor.server.model.UsuarioResumo;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.validation.ProtocoloValidacao;

import java.util.List;
import java.util.stream.Collectors;

public class AdminService {

    private static final String TOKEN_ADMIN = "adm";
    private static final String MSG_DEVE_SER_ADM_LISTA = "Deve ser ADM para consultar a lista";
    private static final String MSG_TOKEN_INVALIDO = "Token Inválido";
    private static final String MSG_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado";
    private static final String MSG_NENHUM_CAMPO_ATUALIZAR = "Informe ao menos nome ou senha para atualizar.";

    private final UsuarioRepository usuarioRepository;

    public AdminService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private static boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isTokenAdminValido(String tokenAdmin) {
        return TOKEN_ADMIN.equals(tokenAdmin);
    }

    private static GenericResponse resposta401(String mensagem) {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(mensagem);
        r.setToken(null);
        return r;
    }

    public Object processarConsultarUsuariosAdmin(ConsultarUsuariosAdminRequest request) {
        String tokenAdmin = request != null ? request.getTokenAdmin() : null;
        if (!isTokenAdminValido(tokenAdmin)) {
            return resposta401(MSG_DEVE_SER_ADM_LISTA);
        }

        List<UsuarioResumo> lista = usuarioRepository.listarTodosUsuarios().stream()
                .map(u -> new UsuarioResumo(u.getUsuario(), u.getNome()))
                .collect(Collectors.toList());

        ConsultarUsuariosAdminResponse ok = new ConsultarUsuariosAdminResponse();
        ok.setResposta("200");
        ok.setListaUsuarios(lista);
        return ok;
    }

    public Object processarConsultarUsuarioAdmin(ConsultarUsuarioAdminRequest request) {
        String tokenAdmin = request != null ? request.getTokenAdmin() : null;
        if (!isTokenAdminValido(tokenAdmin)) {
            return resposta401(MSG_TOKEN_INVALIDO);
        }

        if (request == null || isVazio(request.getUsuario())) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        Usuario cadastrado = usuarioRepository.buscarPorUsuario(request.getUsuario());
        if (cadastrado == null) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        ConsultaUsuarioResponse ok = new ConsultaUsuarioResponse();
        ok.setResposta("200");
        ok.setNome(cadastrado.getNome());
        ok.setUsuario(cadastrado.getUsuario());
        return ok;
    }

    public GenericResponse processarAtualizarUsuarioAdmin(AtualizarUsuarioAdminRequest request) {
        String tokenAdmin = request != null ? request.getTokenAdmin() : null;
        if (!isTokenAdminValido(tokenAdmin)) {
            return resposta401(MSG_TOKEN_INVALIDO);
        }

        if (request == null || isVazio(request.getUsuario())) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        Usuario cadastrado = usuarioRepository.buscarPorUsuario(request.getUsuario());
        if (cadastrado == null) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        boolean alterarNome = !isVazio(request.getNome());
        boolean alterarSenha = !isVazio(request.getSenha());
        if (!alterarNome && !alterarSenha) {
            return resposta401(MSG_NENHUM_CAMPO_ATUALIZAR);
        }

        if (alterarSenha && !ProtocoloValidacao.isSenhaConforme(request.getSenha())) {
            return resposta401(ProtocoloValidacao.MSG_SENHA_FORMATO);
        }

        if (alterarNome) {
            cadastrado.setNome(request.getNome());
        }
        if (alterarSenha) {
            cadastrado.setSenha(request.getSenha());
        }
        usuarioRepository.salvar(cadastrado);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Usuario atualizado com sucesso");
        ok.setToken(null);
        return ok;
    }

    public GenericResponse processarDeletarUsuarioAdmin(DeletarUsuarioAdminRequest request) {
        String tokenAdmin = request != null ? request.getTokenAdmin() : null;
        if (!isTokenAdminValido(tokenAdmin)) {
            return resposta401(MSG_TOKEN_INVALIDO);
        }

        if (request == null || isVazio(request.getUsuario())) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        String loginAlvo = request.getUsuario();
        if (!usuarioRepository.existeUsuario(loginAlvo)) {
            return resposta401(MSG_USUARIO_NAO_ENCONTRADO);
        }

        usuarioRepository.removerTokensPorLogin(loginAlvo);
        usuarioRepository.removerUsuarioPorLogin(loginAlvo);

        GenericResponse ok = new GenericResponse();
        ok.setResposta("200");
        ok.setMensagem("Usuario deletado com sucesso");
        ok.setToken(null);
        return ok;
    }
}
