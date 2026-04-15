package com.vitor.client.beans;

import com.vitor.client.model.GenericResponse;
import com.vitor.client.network.TcpClientService;
import com.vitor.client.service.UsuarioService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;

@Named("usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Inject
    private TcpClientService tcpClientService;
    @Inject
    private UsuarioService usuarioService;
    @Inject
    private ConfiguracaoBean configuracaoBean;

    private String nome;
    private String usuario;
    private String senha;
    /** Preenchido após login com sucesso (exibição na tela). */
    private String tokenRecebido;

    public void executarCadastro() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();

            GenericResponse resp = usuarioService.cadastrar(nome, usuario, senha);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            aplicarMensagemProtocolo(ctx, resp);
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
        }
    }

    public void executarLogin() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        tokenRecebido = null;
        try {
            aplicarServidorTcp();

            GenericResponse resp = usuarioService.login(usuario, senha);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta()) && resp.getToken() != null && !resp.getToken().isBlank()) {
                tokenRecebido = resp.getToken();
            }
            aplicarMensagemProtocolo(ctx, resp);
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
        }
    }

    private void aplicarServidorTcp() {
        tcpClientService.setIp(configuracaoBean.getIp());
        tcpClientService.setPorta(Integer.parseInt(configuracaoBean.getPorta().trim()));
    }

    private static void aplicarMensagemProtocolo(FacesContext ctx, GenericResponse resp) {
        String codigo = resp.getResposta();
        boolean sucesso = "200".equals(codigo);
        boolean erro = !sucesso;
        FacesMessage.Severity severity = erro ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO;
        String summary = sucesso ? "Sucesso" : "Erro";
        String detail = resp.getMensagem() != null ? resp.getMensagem() : "";
        ctx.addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTokenRecebido() {
        return tokenRecebido;
    }

    public void setTokenRecebido(String tokenRecebido) {
        this.tokenRecebido = tokenRecebido;
    }
}
