package com.vitor.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.client.model.AdminRequest;
import com.vitor.client.model.AdminUsuariosResponse;
import com.vitor.client.model.AtualizarUsuarioRequest;
import com.vitor.client.model.CadastroRequest;
import com.vitor.client.model.DeletarUsuarioRequest;
import com.vitor.client.model.EnviarMensagemRequest;
import com.vitor.client.model.ConsultaUsuarioRequest;
import com.vitor.client.model.ConsultaUsuarioPayload;
import com.vitor.client.model.GenericResponse;
import com.vitor.client.model.ListarUsuariosLogadosRequest;
import com.vitor.client.model.LoginRequest;
import com.vitor.client.model.LogoutRequest;
import com.vitor.client.model.UsuariosLogadosResponse;
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

    public AdminUsuariosResponse listarUsuariosAdmin(String tokenAdmin) {
        try {
            AdminRequest request = new AdminRequest();
            request.setOp("consultarUsuariosAdmin");
            request.setTokenAdmin(tokenAdmin);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, AdminUsuariosResponse.class);
        } catch (IOException e) {
            return respostaErroAdminLista(e);
        }
    }

    public ConsultaUsuarioPayload consultarUsuarioAdmin(String tokenAdmin, String usuarioAlvo) {
        try {
            AdminRequest request = new AdminRequest();
            request.setOp("consultarUsuarioAdmin");
            request.setTokenAdmin(tokenAdmin);
            request.setUsuario(usuarioAlvo);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, ConsultaUsuarioPayload.class);
        } catch (IOException e) {
            return respostaErroConsulta(e);
        }
    }

    public GenericResponse atualizarUsuarioAdmin(String tokenAdmin, String usuarioAlvo, String nome, String senha) {
        try {
            AdminRequest request = new AdminRequest();
            request.setOp("atualizarUsuarioAdmin");
            request.setTokenAdmin(tokenAdmin);
            request.setUsuario(usuarioAlvo);
            if (nome != null && !nome.isBlank()) {
                request.setNome(nome);
            }
            if (senha != null && !senha.isBlank()) {
                request.setSenha(senha);
            }

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public GenericResponse deletarUsuarioAdmin(String tokenAdmin, String usuarioAlvo) {
        try {
            AdminRequest request = new AdminRequest();
            request.setOp("deletarUsuarioAdmin");
            request.setTokenAdmin(tokenAdmin);
            request.setUsuario(usuarioAlvo);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public GenericResponse enviarMensagem(String token, String mensagem, String para) {
        try {
            EnviarMensagemRequest request = new EnviarMensagemRequest();
            request.setOp("enviarMensagem");
            request.setToken(token);
            request.setMensagem(mensagem);
            request.setDestinatario(para);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, GenericResponse.class);
        } catch (IOException e) {
            return respostaErroGeneric(e);
        }
    }

    public UsuariosLogadosResponse listarUsuariosLogados(String token) {
        try {
            ListarUsuariosLogadosRequest request = new ListarUsuariosLogadosRequest();
            request.setToken(token);

            String json = MAPPER.writeValueAsString(request);
            return enviarEInterpretar(json, UsuariosLogadosResponse.class);
        } catch (IOException e) {
            UsuariosLogadosResponse erro = new UsuariosLogadosResponse();
            registrarJsonRecebidoEmFalha(e);
            erro.setResposta("erro");
            erro.setMensagem(e.getMessage());
            return erro;
        }
    }

    private <T> T enviarEInterpretar(String json, Class<T> tipoResposta) throws IOException {
        String linha = tcpService.sendRequest(json);
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

    private AdminUsuariosResponse respostaErroAdminLista(IOException e) {
        registrarJsonRecebidoEmFalha(e);
        AdminUsuariosResponse erro = new AdminUsuariosResponse();
        erro.setResposta("erro");
        erro.setMensagem(e.getMessage());
        return erro;
    }

    private void registrarJsonRecebidoEmFalha(IOException e) {
        if (tcpService.getUltimoJsonRecebido() == null) {
            tcpService.setUltimoJsonRecebido(e.getMessage());
        }
    }
}
