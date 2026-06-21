package com.vitor.server.core;

import com.vitor.server.network.ClientHandler;
import com.vitor.server.network.ConexaoManager;
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

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public final class ServerMain {

    private static final int PORTA_PADRAO = 5555;

    private ServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int porta = resolverPorta(args);

        System.out.println("Servidor carregado na porta " + porta);
        System.out.println("Aguardando conexão...\n");

        ServerWindow window = new ServerWindow(porta);
        SwingUtilities.invokeAndWait(() -> window.setVisible(true));

        UsuarioRepository repositorioUsuarios = new UsuarioRepository();
        ConexaoManager conexaoManager = new ConexaoManager(repositorioUsuarios, window);
        CadastroService cadastroService = new CadastroService(repositorioUsuarios);
        LoginService loginService = new LoginService(repositorioUsuarios);
        LogoutService logoutService = new LogoutService(repositorioUsuarios);
        ConsultaService consultaService = new ConsultaService(repositorioUsuarios);
        AtualizarService atualizarService = new AtualizarService(repositorioUsuarios);
        DeletarService deletarService = new DeletarService(repositorioUsuarios);
        AdminService adminService = new AdminService(repositorioUsuarios);
        ListarUsuariosLogadosService listarUsuariosLogadosService =
                new ListarUsuariosLogadosService(repositorioUsuarios, conexaoManager);
        MensagemService mensagemService = new MensagemService(repositorioUsuarios, conexaoManager);

        ServerSocket serverSocket = new ServerSocket(porta);
        try {
            System.out.println("ServerSocket criado. Aceitando conexões em loop.\n");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accept ativado. Nova conexão de "
                        + clientSocket.getRemoteSocketAddress());
                new ClientHandler(clientSocket, cadastroService, loginService, logoutService,
                        consultaService, atualizarService, deletarService, adminService,
                        listarUsuariosLogadosService, mensagemService, conexaoManager,
                        repositorioUsuarios, window).start();
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
