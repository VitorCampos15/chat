package com.vitor.server.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.server.model.AtualizarUsuarioAdminRequest;
import com.vitor.server.model.CadastroRequest;
import com.vitor.server.model.ConsultarUsuarioAdminRequest;
import com.vitor.server.model.ConsultarUsuariosAdminRequest;
import com.vitor.server.model.DeletarUsuarioAdminRequest;
import com.vitor.server.model.EnviarMensagemRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.ListarUsuariosLogadosRequest;
import com.vitor.server.model.LoginRequest;
import com.vitor.server.model.LogoutRequest;
import com.vitor.server.model.AtualizarUsuarioRequest;
import com.vitor.server.model.ConsultaUsuarioRequest;
import com.vitor.server.model.DeletarUsuarioRequest;
import com.vitor.server.model.ConsultaUsuarioResponse;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.service.AdminService;
import com.vitor.server.service.AtualizarService;
import com.vitor.server.service.CadastroService;
import com.vitor.server.service.DeletarService;
import com.vitor.server.service.ConsultaService;
import com.vitor.server.service.ListarUsuariosLogadosService;
import com.vitor.server.service.LoginService;
import com.vitor.server.service.LogoutService;
import com.vitor.server.service.MensagemService;
import com.vitor.server.ui.ServerWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler extends Thread {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket clientSocket;
    private final CadastroService cadastroService;
    private final LoginService loginService;
    private final LogoutService logoutService;
    private final ConsultaService consultaService;
    private final AtualizarService atualizarService;
    private final DeletarService deletarService;
    private final AdminService adminService;
    private final ListarUsuariosLogadosService listarUsuariosLogadosService;
    private final MensagemService mensagemService;
    private final ConexaoManager conexaoManager;
    private final UsuarioRepository usuarioRepository;
    private final ServerWindow serverWindow;

    private PrintWriter out;
    private BufferedReader in;
    private ClienteRede clienteRede;
    private String loginAtivo;
    private final AtomicBoolean registrado = new AtomicBoolean(false);

    public ClientHandler(Socket clientSocket, CadastroService cadastroService, LoginService loginService,
                         LogoutService logoutService, ConsultaService consultaService,
                         AtualizarService atualizarService, DeletarService deletarService,
                         AdminService adminService, ListarUsuariosLogadosService listarUsuariosLogadosService,
                         MensagemService mensagemService, ConexaoManager conexaoManager,
                         UsuarioRepository usuarioRepository, ServerWindow serverWindow) {
        this.clientSocket = clientSocket;
        this.cadastroService = cadastroService;
        this.loginService = loginService;
        this.logoutService = logoutService;
        this.consultaService = consultaService;
        this.atualizarService = atualizarService;
        this.deletarService = deletarService;
        this.adminService = adminService;
        this.listarUsuariosLogadosService = listarUsuariosLogadosService;
        this.mensagemService = mensagemService;
        this.conexaoManager = conexaoManager;
        this.usuarioRepository = usuarioRepository;
        this.serverWindow = serverWindow;
    }

    @Override
    public void run() {
        String remoto = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("[ClientHandler] Thread iniciada para cliente " + remoto);

        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

            clienteRede = extrairClienteRede();

            String linha;
            while ((linha = in.readLine()) != null) {
                System.out.println("Recebido do cliente: " + linha);
                if (serverWindow != null) {
                    serverWindow.atualizarUltimoRecebido(linha);
                }
                processarLinha(linha, remoto, clienteRede);
            }
            System.out.println("[ClientHandler] Fim do stream (cliente desconectou ou fechou envio): " + remoto);
        } catch (IOException e) {
            System.err.println("[ClientHandler] Erro de I/O com " + remoto + ": " + e.getMessage());
        } finally {
            finalizarSessaoCliente();
            fecharRecursos(remoto);
            fecharSocketComSeguranca(remoto);
        }
    }

    public synchronized void enviarLinha(String json) {
        if (out != null) {
            out.println(json);
        }
    }

    public String getLoginAtivo() {
        return loginAtivo;
    }

    private void enviarResposta(String json) {
        if (out != null) {
            out.println(json);
        }
        if (serverWindow != null) {
            serverWindow.atualizarUltimoEnviado(json);
        }
    }

    private void finalizarSessaoCliente() {
        if (registrado.compareAndSet(true, false)) {
            loginAtivo = null;
            conexaoManager.remover(this);
            if (clienteRede != null) {
                usuarioRepository.removerTokensPorRede(clienteRede.ip(), clienteRede.porta());
            }
            conexaoManager.enviarListaUsuariosLogadosParaTodos();
        }
    }

    private ClienteRede extrairClienteRede() {
        String ip = clientSocket.getInetAddress().getHostAddress();
        int porta = clientSocket.getPort();
        return new ClienteRede(ip, porta);
    }

    private void processarLinha(String linha, String remoto, ClienteRede clienteRede) {
        try {
            JsonNode root = MAPPER.readTree(linha);
            JsonNode opNode = root.get("op");
            if (opNode == null || opNode.isNull()) {
                System.err.println("[ClientHandler] ERRO: JSON sem campo 'op'. Cliente: " + remoto);
                enviarResposta(MAPPER.writeValueAsString(respostaErro("Requisição sem operação (op).")));
                return;
            }

            String op = opNode.asText();
            if ("cadastrarUsuario".equals(op)) {
                CadastroRequest request = MAPPER.readValue(linha, CadastroRequest.class);
                GenericResponse resp = cadastroService.processarCadastro(request);
                enviarResposta(MAPPER.writeValueAsString(resp));
                System.out.println("[ClientHandler] OK cadastrarUsuario | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
            } else if ("login".equals(op)) {
                LoginRequest request = MAPPER.readValue(linha, LoginRequest.class);
                GenericResponse resp = loginService.processarLogin(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK login | cliente=" + remoto
                        + " | resposta=" + resp.getResposta()
                        + (resp.getToken() != null ? " | token=(presente)" : " | mensagem=" + resp.getMensagem()));
                if ("200".equals(resp.getResposta())) {
                    loginAtivo = request.getUsuario();
                    registrado.set(true);
                    conexaoManager.registrar(this);
                    conexaoManager.enviarListaUsuariosLogadosParaTodos();
                }
            } else if ("logout".equals(op)) {
                LogoutRequest request = MAPPER.readValue(linha, LogoutRequest.class);
                GenericResponse resp = logoutService.processarLogout(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK logout | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
                if ("200".equals(resp.getResposta()) && registrado.compareAndSet(true, false)) {
                    loginAtivo = null;
                    conexaoManager.remover(this);
                    conexaoManager.enviarListaUsuariosLogadosParaTodos();
                }
            } else if ("enviarMensagem".equals(op)) {
                EnviarMensagemRequest request = MAPPER.readValue(linha, EnviarMensagemRequest.class);
                Object resp = mensagemService.processarEnvio(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK enviarMensagem | cliente=" + remoto
                        + " | resposta=" + extrairRespostaGenerica(resp));
            } else if ("listarUsuariosLogados".equals(op)) {
                ListarUsuariosLogadosRequest request = MAPPER.readValue(linha, ListarUsuariosLogadosRequest.class);
                Object resp = listarUsuariosLogadosService.processarListagem(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK listarUsuariosLogados | cliente=" + remoto
                        + " | resposta=" + extrairRespostaGenerica(resp));
            } else if ("consultarUsuario".equals(op)) {
                ConsultaUsuarioRequest request = MAPPER.readValue(linha, ConsultaUsuarioRequest.class);
                Object resp = consultaService.processarConsulta(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                String resposta = extrairRespostaConsulta(resp);
                System.out.println("[ClientHandler] OK consultarUsuario | cliente=" + remoto
                        + " | resposta=" + resposta);
            } else if ("atualizarUsuario".equals(op)) {
                AtualizarUsuarioRequest request = MAPPER.readValue(linha, AtualizarUsuarioRequest.class);
                GenericResponse resp = atualizarService.processarAtualizacao(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK atualizarUsuario | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
            } else if ("deletarUsuario".equals(op)) {
                DeletarUsuarioRequest request = MAPPER.readValue(linha, DeletarUsuarioRequest.class);
                GenericResponse resp = deletarService.processarExclusao(request, clienteRede);
                String json = MAPPER.writeValueAsString(resp);
                enviarResposta(json);
                System.out.println("[ClientHandler] OK deletarUsuario | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
            } else if ("consultarUsuariosAdmin".equals(op)) {
                ConsultarUsuariosAdminRequest request = MAPPER.readValue(linha, ConsultarUsuariosAdminRequest.class);
                Object resp = adminService.processarConsultarUsuariosAdmin(request);
                enviarJsonAdmin(resp, op, remoto);
            } else if ("consultarUsuarioAdmin".equals(op)) {
                ConsultarUsuarioAdminRequest request = MAPPER.readValue(linha, ConsultarUsuarioAdminRequest.class);
                Object resp = adminService.processarConsultarUsuarioAdmin(request);
                enviarJsonAdmin(resp, op, remoto);
            } else if ("atualizarUsuarioAdmin".equals(op)) {
                AtualizarUsuarioAdminRequest request = MAPPER.readValue(linha, AtualizarUsuarioAdminRequest.class);
                GenericResponse resp = adminService.processarAtualizarUsuarioAdmin(request);
                enviarJsonAdmin(resp, op, remoto);
            } else if ("deletarUsuarioAdmin".equals(op)) {
                DeletarUsuarioAdminRequest request = MAPPER.readValue(linha, DeletarUsuarioAdminRequest.class);
                GenericResponse resp = adminService.processarDeletarUsuarioAdmin(request);
                enviarJsonAdmin(resp, op, remoto);
            } else {
                System.err.println("[ClientHandler] ERRO: operação não suportada '" + op + "'. Cliente: " + remoto);
                enviarResposta(MAPPER.writeValueAsString(respostaErro("Operação não suportada: " + op)));
            }
        } catch (Exception e) {
            System.err.println("[ClientHandler] ERRO ao processar JSON do cliente " + remoto + ": " + e.getMessage());
            e.printStackTrace();
            try {
                enviarResposta(MAPPER.writeValueAsString(respostaErro("JSON inválido ou incompleto.")));
            } catch (Exception ex) {
                System.err.println("[ClientHandler] ERRO ao enviar resposta de erro: " + ex.getMessage());
            }
        }
    }

    private static String extrairRespostaGenerica(Object resp) {
        if (resp instanceof GenericResponse gr) {
            return gr.getResposta();
        }
        if (resp instanceof com.vitor.server.model.UsuariosLogadosResponse ur) {
            return ur.getResposta();
        }
        return "?";
    }

    private static String extrairRespostaConsulta(Object resp) {
        if (resp instanceof GenericResponse gr) {
            return gr.getResposta();
        }
        if (resp instanceof ConsultaUsuarioResponse cr) {
            return cr.getResposta();
        }
        return "?";
    }

    private static GenericResponse respostaErro(String mensagem) {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(mensagem);
        r.setToken(null);
        return r;
    }

    private void enviarJsonAdmin(Object response, String op, String remoto) throws Exception {
        String json = MAPPER.writeValueAsString(response);
        enviarResposta(json);
        System.out.println("[ClientHandler] Enviado ao cliente (" + op + "): " + json);
        System.out.println("[ClientHandler] OK " + op + " | cliente=" + remoto);
    }

    private void fecharRecursos(String remoto) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("[ClientHandler] Falha ao fechar entrada de " + remoto + ": " + e.getMessage());
            }
            in = null;
        }
        if (out != null) {
            out.close();
            out = null;
        }
    }

    private void fecharSocketComSeguranca(String remoto) {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("[ClientHandler] Socket fechado para " + remoto);
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Falha ao fechar socket de " + remoto + ": " + e.getMessage());
        }
    }
}
