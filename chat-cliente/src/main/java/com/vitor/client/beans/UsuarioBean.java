package com.vitor.client.beans;

import com.vitor.client.model.ConsultaUsuarioPayload;
import com.vitor.client.model.GenericResponse;
import com.vitor.client.network.TcpClientService;
import com.vitor.client.service.UsuarioService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;

@Named("usuarioBean")
@SessionScoped
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
    /** Resultado da operação consultarUsuario (somente leitura na UI). */
    private String nomeConsulta;
    private String usuarioConsulta;
    private String novoNome;
    private String novaSenha;

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

    public void executarAtualizacao() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            GenericResponse resp = usuarioService.atualizarUsuario(tokenRecebido, novoNome, novaSenha);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            aplicarMensagemProtocolo(ctx, resp);
            if ("200".equals(resp.getResposta())) {
                novoNome = null;
                novaSenha = null;
            }
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
        }
    }

    public void consultarDados() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        nomeConsulta = null;
        usuarioConsulta = null;
        try {
            aplicarServidorTcp();
            ConsultaUsuarioPayload resp = usuarioService.consultarUsuario(tokenRecebido);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta())) {
                nomeConsulta = resp.getNome();
                usuarioConsulta = resp.getUsuario();
                return;
            }
            if ("401".equals(resp.getResposta())) {
                String detalhe = mensagemDoServidor(resp.getMensagem());
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", detalhe));
                return;
            }
            String detalhe = mensagemDoServidor(resp.getMensagem());
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", detalhe));
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
        }
    }

    public String executarExclusao() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            GenericResponse resp = usuarioService.deletarUsuario(tokenRecebido);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return null;
            }
            if ("200".equals(resp.getResposta())) {
                ctx.getExternalContext().getFlash().setKeepMessages(true);
                String detalhe = mensagemDoServidor(resp.getMensagem());
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", detalhe));
                limparEstadoAposLogout();
                return "/login.xhtml?faces-redirect=true";
            }
            if ("401".equals(resp.getResposta())) {
                String detalhe = mensagemDoServidor(resp.getMensagem());
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", detalhe));
                return null;
            }
            aplicarMensagemProtocolo(ctx, resp);
            return null;
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
            return null;
        }
    }

    public String realizarLogout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            GenericResponse resp = usuarioService.logout(tokenRecebido);
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return null;
            }
            if ("200".equals(resp.getResposta())) {
                ctx.getExternalContext().getFlash().setKeepMessages(true);
                aplicarMensagemProtocolo(ctx, resp);
                limparEstadoAposLogout();
                return "/login.xhtml?faces-redirect=true";
            }
            if ("401".equals(resp.getResposta())) {
                aplicarMensagemProtocolo(ctx, resp);
                return null;
            }
            aplicarMensagemProtocolo(ctx, resp);
            return null;
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida", "Informe um número inteiro válido na barra superior."));
            return null;
        }
    }

    /** Limpa token e dados de formulário; mantém a sessão (ex.: IP/porta em ConfiguracaoBean). */
    private void limparEstadoAposLogout() {
        tokenRecebido = null;
        nome = null;
        usuario = null;
        senha = null;
        nomeConsulta = null;
        usuarioConsulta = null;
        novoNome = null;
        novaSenha = null;
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
        String detail = mensagemDoServidor(resp.getMensagem());
        ctx.addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private static String mensagemDoServidor(String mensagem) {
        return mensagem != null ? mensagem : "";
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

    public String getNomeConsulta() {
        return nomeConsulta;
    }

    public void setNomeConsulta(String nomeConsulta) {
        this.nomeConsulta = nomeConsulta;
    }

    public String getUsuarioConsulta() {
        return usuarioConsulta;
    }

    public void setUsuarioConsulta(String usuarioConsulta) {
        this.usuarioConsulta = usuarioConsulta;
    }

    public String getNovoNome() {
        return novoNome;
    }

    public void setNovoNome(String novoNome) {
        this.novoNome = novoNome;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
