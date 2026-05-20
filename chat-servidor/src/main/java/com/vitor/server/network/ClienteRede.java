package com.vitor.server.network;

public record ClienteRede(String ip, int porta) {

    public ClienteRede {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP do cliente é obrigatório.");
        }
    }
}
