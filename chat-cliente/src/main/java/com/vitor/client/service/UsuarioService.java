package com.vitor.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.client.model.AtualizarUsuarioRequest;
import com.vitor.client.model.CadastroRequest;
import com.vitor.client.model.DeletarUsuarioRequest;
import com.vitor.client.model.ConsultaUsuarioRequest;
import com.vitor.client.model.ConsultaUsuarioPayload;
import com.vitor.client.model.GenericResponse;
import com.vitor.client.model.LoginRequest;
import com.vitor.client.model.LogoutRequest;
import com.vitor.client.network.TcpClientService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.io.IOException;

@RequestScoped
public class UsuarioService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Inject
    private TcpClientService tcpService;

    public GenericResponse cadastrar(String nome, String user, String senha) {
        try {
            CadastroRequest request = new CadastroRequest();
            request.setOp("cadastrarUsuario");
            request.setNome(nome);
            request.setUsuario(user);
            request.setSenha(senha);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, GenericResponse.class);
        } catch (IOException e) {
            GenericResponse erro = new GenericResponse();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    public GenericResponse login(String usuario, String senha) {
        try {
            LoginRequest request = new LoginRequest();
            request.setOp("login");
            request.setUsuario(usuario);
            request.setSenha(senha);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, GenericResponse.class);
        } catch (IOException e) {
            GenericResponse erro = new GenericResponse();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    public GenericResponse logout(String token) {
        try {
            LogoutRequest request = new LogoutRequest();
            request.setOp("logout");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, GenericResponse.class);
        } catch (IOException e) {
            GenericResponse erro = new GenericResponse();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    public ConsultaUsuarioPayload consultarUsuario(String token) {
        try {
            ConsultaUsuarioRequest request = new ConsultaUsuarioRequest();
            request.setOp("consultarUsuario");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, ConsultaUsuarioPayload.class);
        } catch (IOException e) {
            ConsultaUsuarioPayload erro = new ConsultaUsuarioPayload();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    public GenericResponse atualizarUsuario(String token, String nome, String senha) {
        try {
            AtualizarUsuarioRequest request = new AtualizarUsuarioRequest();
            request.setOp("atualizarUsuario");
            request.setToken(token);
            request.setNome(nome);
            request.setSenha(senha);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, GenericResponse.class);
        } catch (IOException e) {
            GenericResponse erro = new GenericResponse();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    public GenericResponse deletarUsuario(String token) {
        try {
            DeletarUsuarioRequest request = new DeletarUsuarioRequest();
            request.setOp("deletarUsuario");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            String linha = tcpService.sendRequest(json);
            return MAPPER.readValue(linha, GenericResponse.class);
        } catch (IOException e) {
            GenericResponse erro = new GenericResponse();
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }
}
