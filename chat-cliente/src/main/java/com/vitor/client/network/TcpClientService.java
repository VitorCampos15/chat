package com.vitor.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import jakarta.enterprise.context.RequestScoped;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@RequestScoped
public class TcpClientService {

    private String ip;
    private int porta;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String sendRequest(String json) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, porta), 5000);
            socket.setSoTimeout(5000);

            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                out.println(json);

                String line = in.readLine();
                if (line == null) {
                    throw new IOException("O servidor fechou a conexão inesperadamente.");
                }
                return line;
            }
        }
    }
}
