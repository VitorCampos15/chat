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

    private String ultimoJsonEnviado;
    private String ultimoJsonRecebido;

    public String getUltimoJsonEnviado() {
        return ultimoJsonEnviado;
    }

    public String getUltimoJsonRecebido() {
        return ultimoJsonRecebido;
    }

    public GenericResponse cadastrar(String nome, String user, String senha) {
        try {
            CadastroRequest request = new CadastroRequest();
            request.setOp("cadastrarUsuario");
            request.setNome(nome);
            request.setUsuario(user);
            request.setSenha(senha);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public GenericResponse login(String usuario, String senha) {
        try {
            LoginRequest request = new LoginRequest();
            request.setOp("login");
            request.setUsuario(usuario);
            request.setSenha(senha);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public GenericResponse logout(String token) {
        try {
            LogoutRequest request = new LogoutRequest();
            request.setOp("logout");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public ConsultaUsuarioPayload consultarUsuario(String token) {
        try {
            ConsultaUsuarioRequest request = new ConsultaUsuarioRequest();
            request.setOp("consultarUsuario");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, ConsultaUsuarioPayload.class);
        } catch (IOException e) {
            return respostaErroConsulta(e);
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
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public GenericResponse deletarUsuario(String token) {
        try {
            DeletarUsuarioRequest request = new DeletarUsuarioRequest();
            request.setOp("deletarUsuario");
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    private <T> T enviarEInterpretar(String json, Class<T> tipoResposta) throws IOException {
        ultimoJsonEnviado = json;
        String linha = tcpService.sendRequest(json);
        ultimoJsonRecebido = linha;
        return MAPPER.readValue(linha, tipoResposta);
    }

    private GenericResponse respostaErroGeneric(IOException e) {
        registrarJsonRecebidoEmFalha(e);
        GenericResponse erro = new GenericResponse();
        erro.setResposta("erro");
        erro.setMensagem(e.getMessage());
        return erro;
    }

    private ConsultaUsuarioPayload respostaErroConsulta(IOException e) {
        registrarJsonRecebidoEmFalha(e);
        ConsultaUsuarioPayload erro = new ConsultaUsuarioPayload();
        erro.setResposta("erro");
        erro.setMensagem(e.getMessage());
        return erro;
    }

    private void registrarJsonRecebidoEmFalha(IOException e) {
        if (ultimoJsonRecebido == null) {
            ultimoJsonRecebido = e.getMessage();
        }
    }
}
