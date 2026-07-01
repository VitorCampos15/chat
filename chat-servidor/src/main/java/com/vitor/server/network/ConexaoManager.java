package com.vitor.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.server.model.UsuariosLogadosResponse;
import com.vitor.server.repository.UsuarioRepository;
import com.vitor.server.ui.ServerWindow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConexaoManager {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UsuarioRepository usuarioRepository;
    private final ServerWindow serverWindow;
    private final CopyOnWriteArrayList<ClientHandler> clientesConectados = new CopyOnWriteArrayList<>();

    public ConexaoManager(UsuarioRepository usuarioRepository, ServerWindow serverWindow) {
        this.usuarioRepository = usuarioRepository;
        this.serverWindow = serverWindow;
    }

    public void registrar(ClientHandler handler) {
        if (handler != null) {
            clientesConectados.addIfAbsent(handler);
            atualizarTabelaUsuariosLogados();
        }
    }

    public void remover(ClientHandler handler) {
        if (handler != null) {
            clientesConectados.remove(handler);
            atualizarTabelaUsuariosLogados();
        }
    }

    public boolean usuarioEstaConectado(String login) {
        return buscarPorLogin(login) != null;
    }

    public ClientHandler buscarPorLogin(String login) {
        if (login == null) {
            return null;
        }
        for (ClientHandler handler : clientesConectados) {
            if (login.equals(handler.getLoginAtivo())) {
                return handler;
            }
        }
        return null;
    }

    public UsuariosLogadosResponse montarRespostaUsuariosLogados() {
        List<String> logins = usuarioRepository.listarLoginsAtivos();
        UsuariosLogadosResponse resp = new UsuariosLogadosResponse();
        resp.setResposta("200");
        resp.setUsuariosLogados(logins);
        return resp;
    }

    public void enviarListaUsuariosLogadosParaTodos() {
        try {
            String json = MAPPER.writeValueAsString(montarRespostaUsuariosLogados());
            System.out.println("[SERVER -> BROADCAST] Enviando lista de logados: " + json);
            if (serverWindow != null) {
                serverWindow.atualizarUltimoEnviado(json);
            }
            for (ClientHandler cliente : clientesConectados) {
                cliente.enviarLinha(json);
            }
            atualizarTabelaUsuariosLogados();
        } catch (Exception e) {
            System.err.println("[ConexaoManager] Falha ao enviar broadcast de usuários logados: " + e.getMessage());
        }
    }

    public void enviarMensagemParaTodos(String json, String remetenteExcluir) {
        System.out.println("[SERVER -> BROADCAST MENSAGEM] " + json);
        if (serverWindow != null) {
            serverWindow.atualizarUltimoEnviado(json);
        }
        for (ClientHandler cliente : clientesConectados) {
            String login = cliente.getLoginAtivo();
            if (remetenteExcluir != null && remetenteExcluir.equals(login)) {
                continue;
            }
            cliente.enviarLinha(json);
        }
    }

    public void enviarMensagemPrivada(String loginDestino, String json) {
        System.out.println("[SERVER -> MENSAGEM PRIVADA para " + loginDestino + "] " + json);
        if (serverWindow != null) {
            serverWindow.atualizarUltimoEnviado(json);
        }
        ClientHandler destinatario = buscarPorLogin(loginDestino);
        if (destinatario != null) {
            destinatario.enviarLinha(json);
        }
    }

    private void atualizarTabelaUsuariosLogados() {
        if (serverWindow != null) {
            serverWindow.atualizarTabela(usuarioRepository.listarSessoesAtivasDetalhadas());
        }
    }
}
