package com.vitor.client.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;

@Named("configuracaoBean")
@SessionScoped
public class ConfiguracaoBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String ip = "127.0.0.1";
    private String porta = "12345";

    public void salvarConfiguracao() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Configuração", "IP e porta atualizados para esta sessão."));
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPorta() {
        return porta;
    }

    public void setPorta(String porta) {
        this.porta = porta;
    }
}
