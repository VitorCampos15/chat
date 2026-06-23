package com.vitor.client.beans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.client.model.AdminUsuariosResponse;
import com.vitor.client.model.ConsultaUsuarioPayload;
import com.vitor.client.model.GenericResponse;
import com.vitor.client.model.MensagemDTO;
import com.vitor.client.model.MensagemEvent;
import com.vitor.client.model.UsuarioDTO;
import com.vitor.client.model.UsuariosLogadosResponse;
import com.vitor.client.network.TcpClientService;
import com.vitor.client.service.UsuarioService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("usuarioBean")
@SessionScoped
public class UsuarioBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DESTINO_TODOS = "/todos";

    @Inject
    private TcpClientService tcpClientService;
    @Inject
    private UsuarioService usuarioService;
    @Inject
    private ConfiguracaoBean configuracaoBean;

    private String nome;
    private String usuario;
    private String senha;
    /** Token enviado nas operações autenticadas; editável na barra superior para testes. */
    private String tokenRecebido;
    /** Resultado da operação consultarUsuario (somente leitura na UI). */
    private String nomeConsulta;
    private String usuarioConsulta;
    private String novoNome;
    private String novaSenha;
    /** Último JSON enviado ao servidor (exibição na apresentação). */
    private String jsonEnviado;
    /** Última linha JSON recebida do servidor (exibição na apresentação). */
    private String jsonRecebido;
    private List<UsuarioDTO> listaUsuariosAdmin = new ArrayList<>();
    private String usuarioAlvo;
    private String nomeAlvo;
    private String senhaAlvo;
    private List<String> usuariosLogados = new ArrayList<>();
    private List<MensagemDTO> historicoMensagens = new ArrayList<>();
    private String usuarioLogado;
    private String textoMensagem;
    private String destinatario;

    public void executarCadastro() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();

            GenericResponse resp = usuarioService.cadastrar(nome, usuario, senha);
            registrarJsonsDaUltimaChamada();
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
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta()) && resp.getToken() != null && !resp.getToken().isBlank()) {
                tokenRecebido = resp.getToken();
                usuarioLogado = usuario;
                tcpClientService.setPushHandler(this::processarPushServidor);
                tcpClientService.iniciarOuvinte();
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
            registrarJsonsDaUltimaChamada();
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
            registrarJsonsDaUltimaChamada();
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
            registrarJsonsDaUltimaChamada();
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
            registrarJsonsDaUltimaChamada();
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
        tcpClientService.pararOuvinte();
        tokenRecebido = null;
        nome = null;
        usuario = null;
        senha = null;
        nomeConsulta = null;
        usuarioConsulta = null;
        novoNome = null;
        novaSenha = null;
        usuariosLogados.clear();
        historicoMensagens.clear();
        usuarioLogado = null;
        textoMensagem = null;
        destinatario = null;
    }

    public void enviarMensagem() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (tokenRecebido == null || tokenRecebido.isBlank()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Faça login para enviar mensagens."));
            return;
        }
        if (textoMensagem == null || textoMensagem.isBlank()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Digite uma mensagem."));
            return;
        }
        if (destinatario == null || destinatario.isBlank()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Digite o nome do destinatário."));
            return;
        }
        try {
            aplicarServidorTcp();
            String destino = normalizarDestinatario(destinatario.trim());
            GenericResponse resp = usuarioService.enviarMensagem(tokenRecebido, textoMensagem.trim(), destino);
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta())) {
                textoMensagem = null;
                return;
            }
            aplicarMensagemProtocolo(ctx, resp);
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida",
                    "Informe um número inteiro válido na barra superior."));
        }
    }

    private synchronized void processarPushServidor(String json) {
        jsonRecebido = json;
        try {
            if (isListaUsuariosOnlinePush(json)) {
                UsuariosLogadosResponse resp = MAPPER.readValue(json, UsuariosLogadosResponse.class);
                if (resp.getUsuariosLogados() != null) {
                    usuariosLogados = new ArrayList<>(resp.getUsuariosLogados());
                } else {
                    usuariosLogados.clear();
                }
                return;
            }
            MensagemEvent evento = MAPPER.readValue(json, MensagemEvent.class);
            if (evento.getMensagem() != null) {
                historicoMensagens.add(new MensagemDTO(evento.getDe(), evento.getDestinatario(), evento.getMensagem()));
            }
        } catch (Exception ignored) {
            // Mantém jsonRecebido atualizado para auditoria mesmo em JSON não mapeado.
        }
    }

    private boolean isListaUsuariosOnlinePush(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (!root.has("lista_usuarios")) {
                return false;
            }
            JsonNode lista = root.get("lista_usuarios");
            if (!lista.isArray()) {
                return false;
            }
            if (lista.isEmpty()) {
                return true;
            }
            return lista.get(0).isTextual();
        } catch (Exception e) {
            return false;
        }
    }

    public String formatarMensagem(MensagemDTO msg) {
        if (msg == null) {
            return "";
        }
        String de = msg.getDe() != null ? msg.getDe() : "?";
        String para = msg.getPara() != null ? msg.getPara() : "?";
        if (isDestinoTodos(para)) {
            para = "Todos";
        }
        String texto = msg.getMensagem() != null ? msg.getMensagem() : "";
        return "[" + de + " para " + para + "]: " + texto;
    }

    private static String normalizarDestinatario(String destino) {
        if (destino == null || destino.isBlank()) {
            return destino;
        }
        if ("todos".equalsIgnoreCase(destino.trim())) {
            return DESTINO_TODOS;
        }
        return destino.trim();
    }

    private static boolean isDestinoTodos(String para) {
        if (para == null) {
            return false;
        }
        String valor = para.trim();
        return DESTINO_TODOS.equals(valor) || "todos".equalsIgnoreCase(valor);
    }

    public void carregarListaAdmin() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            AdminUsuariosResponse resp = usuarioService.listarUsuariosAdmin(tokenRecebido);
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta()) && resp.getListaUsuarios() != null) {
                listaUsuariosAdmin = new ArrayList<>(resp.getListaUsuarios());
                return;
            }
            if ("401".equals(resp.getResposta())) {
                listaUsuariosAdmin.clear();
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        mensagemDoServidor(resp.getMensagem())));
                return;
            }
            listaUsuariosAdmin.clear();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    mensagemDoServidor(resp.getMensagem())));
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida",
                    "Informe um número inteiro válido na barra superior."));
        }
    }

    public void consultarAlvo() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        nomeAlvo = null;
        try {
            aplicarServidorTcp();
            ConsultaUsuarioPayload resp = usuarioService.consultarUsuarioAdmin(tokenRecebido, usuarioAlvo);
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta())) {
                nomeAlvo = resp.getNome();
                if (resp.getUsuario() != null) {
                    usuarioAlvo = resp.getUsuario();
                }
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Dados do usuário carregados."));
                return;
            }
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                    mensagemDoServidor(resp.getMensagem())));
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida",
                    "Informe um número inteiro válido na barra superior."));
        }
    }

    public void atualizarAlvo() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            GenericResponse resp = usuarioService.atualizarUsuarioAdmin(
                    tokenRecebido, usuarioAlvo, nomeAlvo, senhaAlvo);
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta())) {
                aplicarMensagemProtocolo(ctx, resp);
                senhaAlvo = null;
                return;
            }
            if ("401".equals(resp.getResposta())) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        mensagemDoServidor(resp.getMensagem())));
                return;
            }
            aplicarMensagemProtocolo(ctx, resp);
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida",
                    "Informe um número inteiro válido na barra superior."));
        }
    }

    public void deletarAlvo() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            aplicarServidorTcp();
            GenericResponse resp = usuarioService.deletarUsuarioAdmin(tokenRecebido, usuarioAlvo);
            registrarJsonsDaUltimaChamada();
            if (resp == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Resposta vazia do servidor."));
                return;
            }
            if ("200".equals(resp.getResposta())) {
                aplicarMensagemProtocolo(ctx, resp);
                nomeAlvo = null;
                senhaAlvo = null;
                return;
            }
            if ("401".equals(resp.getResposta())) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                        mensagemDoServidor(resp.getMensagem())));
                return;
            }
            aplicarMensagemProtocolo(ctx, resp);
        } catch (NumberFormatException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Porta inválida",
                    "Informe um número inteiro válido na barra superior."));
        }
    }

    public void desconectarTcp() {
        tcpClientService.disconnect();
        usuariosLogados.clear();
        historicoMensagens.clear();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "TCP", "Conexão com o servidor encerrada."));
    }

    public boolean isTcpConectado() {
        return tcpClientService.isConectado();
    }

    private void aplicarServidorTcp() {
        tcpClientService.setIp(configuracaoBean.getIp());
        tcpClientService.setPorta(Integer.parseInt(configuracaoBean.getPorta().trim()));
    }

    private void registrarJsonsDaUltimaChamada() {
        String enviado = tcpClientService.getUltimoJsonEnviado();
        String recebido = tcpClientService.getUltimoJsonRecebido();
        this.jsonEnviado = enviado != null ? enviado : "";
        this.jsonRecebido = recebido != null ? recebido : "";
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

    public String getJsonEnviado() {
        return jsonEnviado;
    }

    public void setJsonEnviado(String jsonEnviado) {
        this.jsonEnviado = jsonEnviado;
    }

    public String getJsonRecebido() {
        return jsonRecebido;
    }

    public void setJsonRecebido(String jsonRecebido) {
        this.jsonRecebido = jsonRecebido;
    }

    public List<UsuarioDTO> getListaUsuariosAdmin() {
        return listaUsuariosAdmin;
    }

    public void setListaUsuariosAdmin(List<UsuarioDTO> listaUsuariosAdmin) {
        this.listaUsuariosAdmin = listaUsuariosAdmin != null ? listaUsuariosAdmin : new ArrayList<>();
    }

    public String getUsuarioAlvo() {
        return usuarioAlvo;
    }

    public void setUsuarioAlvo(String usuarioAlvo) {
        this.usuarioAlvo = usuarioAlvo;
    }

    public String getNomeAlvo() {
        return nomeAlvo;
    }

    public void setNomeAlvo(String nomeAlvo) {
        this.nomeAlvo = nomeAlvo;
    }

    public String getSenhaAlvo() {
        return senhaAlvo;
    }

    public void setSenhaAlvo(String senhaAlvo) {
        this.senhaAlvo = senhaAlvo;
    }

    public List<String> getUsuariosLogados() {
        return usuariosLogados;
    }

    public void setUsuariosLogados(List<String> usuariosLogados) {
        this.usuariosLogados = usuariosLogados != null ? usuariosLogados : new ArrayList<>();
    }

    public List<MensagemDTO> getHistoricoMensagens() {
        if (historicoMensagens == null) {
            return new ArrayList<>();
        }
        return historicoMensagens;
    }

    public void setHistoricoMensagens(List<MensagemDTO> historicoMensagens) {
        this.historicoMensagens = historicoMensagens != null ? historicoMensagens : new ArrayList<>();
    }

    public String getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(String usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public String getTextoMensagem() {
        return textoMensagem;
    }

    public void setTextoMensagem(String textoMensagem) {
        this.textoMensagem = textoMensagem;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }
}
