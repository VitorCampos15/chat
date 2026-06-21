package com.vitor.client.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SessionScoped
public class TcpClientService implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long TIMEOUT_RESPOSTA_MS = 30_000L;

    private String ip;
    private int porta;

    private String lastConnectedIp;
    private int lastConnectedPort = -1;

    private String ultimoJsonEnviado;
    private String ultimoJsonRecebido;

    private transient Socket socket;
    private transient PrintWriter out;
    private transient BufferedReader in;
    private transient Thread ouvinteThread;
    private transient BlockingQueue<String> filaRespostas;
    private transient Consumer<String> pushHandler;

    private volatile boolean ouvinteAtivo;

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

    public String getUltimoJsonEnviado() {
        return ultimoJsonEnviado;
    }

    public String getUltimoJsonRecebido() {
        return ultimoJsonRecebido;
    }

    public void setUltimoJsonRecebido(String ultimoJsonRecebido) {
        this.ultimoJsonRecebido = ultimoJsonRecebido;
    }

    public void setPushHandler(Consumer<String> pushHandler) {
        this.pushHandler = pushHandler;
    }

    public void iniciarOuvinte() {
        synchronized (this) {
            if (ouvinteAtivo) {
                return;
            }
            try {
                conectarSeNecessario();
            } catch (IOException e) {
                ultimoJsonRecebido = e.getMessage();
                return;
            }
            if (filaRespostas == null) {
                filaRespostas = new LinkedBlockingQueue<>();
            }
            ouvinteAtivo = true;
            ouvinteThread = new Thread(this::loopOuvinte, "tcp-client-listener");
            ouvinteThread.setDaemon(true);
            ouvinteThread.start();
        }
    }

    public void pararOuvinte() {
        synchronized (this) {
            ouvinteAtivo = false;
            if (ouvinteThread != null) {
                ouvinteThread.interrupt();
                ouvinteThread = null;
            }
            if (filaRespostas != null) {
                filaRespostas.clear();
            }
            pushHandler = null;
        }
    }

    public String sendRequest(String json) throws IOException {
        synchronized (this) {
            ultimoJsonEnviado = json;
            fecharSeEndpointMudou();
            conectarSeNecessario();
            try {
                out.println(json);
                String line;
                if (ouvinteAtivo) {
                    if (filaRespostas == null) {
                        filaRespostas = new LinkedBlockingQueue<>();
                    }
                    line = filaRespostas.poll(TIMEOUT_RESPOSTA_MS, TimeUnit.MILLISECONDS);
                    if (line == null) {
                        throw new IOException("Timeout aguardando resposta do servidor.");
                    }
                } else {
                    line = in.readLine();
                    if (line == null) {
                        fecharRecursos();
                        throw new IOException("O servidor fechou a conexão inesperadamente.");
                    }
                }
                ultimoJsonRecebido = line;
                return line;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fecharRecursos();
                throw new IOException("Leitura interrompida aguardando resposta do servidor.", e);
            } catch (IOException e) {
                fecharRecursos();
                if (ultimoJsonRecebido == null) {
                    ultimoJsonRecebido = e.getMessage();
                }
                throw e;
            }
        }
    }

    public void disconnect() {
        synchronized (this) {
            pararOuvinte();
            fecharRecursos();
        }
    }

    @PreDestroy
    public void aoDestruirSessao() {
        disconnect();
    }

    private void loopOuvinte() {
        while (ouvinteAtivo) {
            try {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if (isPushMessage(line)) {
                    ultimoJsonRecebido = line;
                    Consumer<String> handler = pushHandler;
                    if (handler != null) {
                        handler.accept(line);
                    }
                } else if (filaRespostas != null) {
                    filaRespostas.offer(line);
                }
            } catch (IOException e) {
                if (ouvinteAtivo) {
                    ultimoJsonRecebido = e.getMessage();
                }
                break;
            }
        }
        synchronized (this) {
            ouvinteAtivo = false;
        }
    }

    private static boolean isPushMessage(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root.has("usuarios_logados")) {
                return true;
            }
            return root.has("mensagem") && root.has("de") && root.has("para");
        } catch (Exception ignored) {
            return json.contains("\"usuarios_logados\"")
                    || (json.contains("\"mensagem\"") && json.contains("\"de\""));
        }
    }

    private void fecharSeEndpointMudou() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        boolean mudou = lastConnectedPort != porta
                || !Objects.equals(lastConnectedIp, ip);
        if (mudou) {
            pararOuvinte();
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
        novo.setSoTimeout(0);
        socket = novo;
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        lastConnectedIp = ip;
        lastConnectedPort = porta;
    }

    private void fecharRecursos() {
        ouvinteAtivo = false;
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
        if (filaRespostas != null) {
            filaRespostas.clear();
        }
    }
}
