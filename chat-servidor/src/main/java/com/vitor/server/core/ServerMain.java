package com.vitor.server.core;

import com.vitor.server.network.ClientHandler;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.service.CadastroService;
import com.vitor.server.service.LoginService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerMain {

    /** Usada se a entrada da porta estiver vazia (Enter sem número). */
    private static final int PORTA_PADRAO = 5555;

    private static final UsuarioRepository REPOSITORIO_USUARIOS = new UsuarioRepository();
    private static final CadastroService CADASTRO_SERVICE = new CadastroService(REPOSITORIO_USUARIOS);
    private static final LoginService LOGIN_SERVICE = new LoginService(REPOSITORIO_USUARIOS);

    private ServerMain() {
    }

    public static void main(String[] args) throws IOException {
        int porta = resolverPorta(args);

        System.out.println("Servidor carregado na porta " + porta);
        System.out.println("Aguardando conexão...\n");

        ServerSocket serverSocket = new ServerSocket(porta);
        try {
            System.out.println("ServerSocket criado. Aceitando conexões em loop.\n");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accept ativado. Nova conexão de "
                        + clientSocket.getRemoteSocketAddress());
                new ClientHandler(clientSocket, CADASTRO_SERVICE, LOGIN_SERVICE).start();
            }
        } catch (IOException e) {
            System.err.println("Falha no accept ou no ServerSocket: " + e.getMessage());
            throw e;
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar ServerSocket: " + e.getMessage());
            }
        }
    }

    private static int resolverPorta(String[] args) throws IOException {
        if (args.length > 0 && !args[0].isBlank()) {
            return Integer.parseInt(args[0].trim());
        }
        System.out.println("Qual porta o servidor deve usar? (Enter para " + PORTA_PADRAO + ")");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String linha = br.readLine();
        if (linha == null || linha.isBlank()) {
            return PORTA_PADRAO;
        }
        return Integer.parseInt(linha.trim());
    }
}
