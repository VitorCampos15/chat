package com.vitor.client.model;

import java.io.Serial;
import java.io.Serializable;

public class MensagemDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String de;
    private String para;
    private String mensagem;

    public MensagemDTO() {
    }

    public MensagemDTO(String de, String para, String mensagem) {
        this.de = de;
        this.para = para;
        this.mensagem = mensagem;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public String getPara() {
        return para;
    }

    public void setPara(String para) {
        this.para = para;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTextoExibicao(String meuUsuario) {
        String destinoExibicao;
        if (para != null && ("/todos".equals(para.trim()) || "todos".equalsIgnoreCase(para.trim()))) {
            destinoExibicao = "Todos";
        } else if (meuUsuario != null && meuUsuario.equals(para)) {
            destinoExibicao = "Você";
        } else {
            destinoExibicao = para != null ? para : "?";
        }
        String remetente = de != null ? de : "?";
        return "[" + remetente + " para " + destinoExibicao + "]: " + (mensagem != null ? mensagem : "");
    }
}
