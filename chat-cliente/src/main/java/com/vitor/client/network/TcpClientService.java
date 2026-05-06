package com.vitor.client.network;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@SessionScoped
public class TcpClientService implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String ip;
    private int porta;

    private String lastConnectedIp;
    private int lastConnectedPort = -1;

    private transient Socket socket;
    private transient PrintWriter out;
    private transient BufferedReader in;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public boolean isConectado() {
        synchronized (this) {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }

    public String sendRequest(String json) throws IOException {
        synchronized (this) {
            fecharSeEndpointMudou();
            conectarSeNecessario();
            try {
                out.println(json);
                String line = in.readLine();
                if (line == null) {
                    fecharRecursos();
                    throw new IOException("O servidor fechou a conexão inesperadamente.");
                }
                return line;
            } catch (IOException e) {
                fecharRecursos();
                throw e;
            }
        }
    }

    public void disconnect() {
        synchronized (this) {
            fecharRecursos();
        }
    }

    @PreDestroy
    public void aoDestruirSessao() {
        disconnect();
    }

    private void fecharSeEndpointMudou() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        boolean mudou = lastConnectedPort != porta
                || !Objects.equals(lastConnectedIp, ip);
        if (mudou) {
            fecharRecursos();
        }
    }

    private void conectarSeNecessario() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        if (ip == null || ip.isBlank()) {
            throw new IOException("IP do servidor não configurado.");
        }
        Socket novo = new Socket();
        novo.connect(new InetSocketAddress(ip.trim(), porta), 5000);
        novo.setSoTimeout(5000);
        socket = novo;
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        lastConnectedIp = ip;
        lastConnectedPort = porta;
    }

    private void fecharRecursos() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignored) {
            }
            in = null;
        }
        if (out != null) {
            out.close();
            out = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
        }
        lastConnectedIp = null;
        lastConnectedPort = -1;
    }
}

