package com.vitor.server.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.server.model.CadastroRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.LoginRequest;
import com.vitor.server.service.CadastroService;
import com.vitor.server.service.LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket clientSocket;
    private final CadastroService cadastroService;
    private final LoginService loginService;

    public ClientHandler(Socket clientSocket, CadastroService cadastroService, LoginService loginService) {
        this.clientSocket = clientSocket;
        this.cadastroService = cadastroService;
        this.loginService = loginService;
    }

    @Override
    public void run() {
        String remoto = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("[ClientHandler] Thread iniciada para cliente " + remoto);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String linha;
            while ((linha = in.readLine()) != null) {
                System.out.println("Recebido do cliente: " + linha);
                processarLinha(linha, remoto, out);
            }
            System.out.println("[ClientHandler] Fim do stream (cliente desconectou ou fechou envio): " + remoto);
        } catch (IOException e) {
            System.err.println("[ClientHandler] Erro de I/O com " + remoto + ": " + e.getMessage());
        } finally {
            fecharSocketComSeguranca(remoto);
        }
    }

    private void processarLinha(String linha, String remoto, PrintWriter out) {
        try {
            JsonNode root = MAPPER.readTree(linha);
            JsonNode opNode = root.get("op");
            if (opNode == null || opNode.isNull()) {
                System.err.println("[ClientHandler] ERRO: JSON sem campo 'op'. Cliente: " + remoto);
                enviarJson(out, respostaErro("Requisição sem operação (op)."));
                return;
            }

            String op = opNode.asText();
            if ("cadastrarUsuario".equals(op)) {
                CadastroRequest request = MAPPER.readValue(linha, CadastroRequest.class);
                GenericResponse resp = cadastroService.processarCadastro(request);
                String json = MAPPER.writeValueAsString(resp);
                out.println(json);
                System.out.println("[ClientHandler] OK cadastrarUsuario | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
            } else if ("login".equals(op)) {
                LoginRequest request = MAPPER.readValue(linha, LoginRequest.class);
                GenericResponse resp = loginService.processarLogin(request);
                String json = MAPPER.writeValueAsString(resp);
                out.println(json);
                System.out.println("[ClientHandler] OK login | cliente=" + remoto
                        + " | resposta=" + resp.getResposta() + " | mensagem=" + resp.getMensagem());
            } else {
                System.err.println("[ClientHandler] ERRO: operação não suportada '" + op + "'. Cliente: " + remoto);
                enviarJson(out, respostaErro("Operação não suportada: " + op));
            }
        } catch (Exception e) {
            System.err.println("[ClientHandler] ERRO ao processar JSON do cliente " + remoto + ": " + e.getMessage());
            e.printStackTrace();
            try {
                enviarJson(out, respostaErro("JSON inválido ou incompleto."));
            } catch (Exception ex) {
                System.err.println("[ClientHandler] ERRO ao enviar resposta de erro: " + ex.getMessage());
            }
        }
    }

    private static GenericResponse respostaErro(String mensagem) {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(mensagem);
        r.setToken("");
        return r;
    }

    private static void enviarJson(PrintWriter out, GenericResponse response) throws Exception {
        out.println(MAPPER.writeValueAsString(response));
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
