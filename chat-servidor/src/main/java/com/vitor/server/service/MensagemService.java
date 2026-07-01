package com.vitor.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitor.server.model.EnviarMensagemRequest;
import com.vitor.server.model.GenericResponse;
import com.vitor.server.model.MensagemEvent;
import com.vitor.server.network.ClienteRede;
import com.vitor.server.network.ConexaoManager;
import com.vitor.server.repository.UsuarioRepository;

public class MensagemService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DESTINO_TODOS = "/todos";
    private static final String MSG_TOKEN_INVALIDO = "Token inválido";

    private final UsuarioRepository usuarioRepository;
    private final ConexaoManager conexaoManager;

    public MensagemService(UsuarioRepository usuarioRepository, ConexaoManager conexaoManager) {
        this.usuarioRepository = usuarioRepository;
        this.conexaoManager = conexaoManager;
    }

    public Object processarEnvio(EnviarMensagemRequest request, ClienteRede clienteRede) {
        String token = request != null ? request.getToken() : null;
        String texto = request != null ? request.getMensagem() : null;
        String para = request != null ? request.getDestinatario() : null;

        if (token == null || token.isBlank() || clienteRede == null) {
            return respostaErro(MSG_TOKEN_INVALIDO);
        }
        if (texto == null || texto.isBlank()) {
            return respostaErro("Mensagem obrigatória.");
        }
        if (para == null || para.isBlank()) {
            return respostaErro("Destinatário obrigatório.");
        }

        String remetente = usuarioRepository.obterLoginPorToken(token, clienteRede.ip(), clienteRede.porta());
        if (remetente == null) {
            return respostaErro(MSG_TOKEN_INVALIDO);
        }

        try {
            MensagemEvent evento = new MensagemEvent();
            evento.setOp("receberMensagem");
            evento.setRemetente(remetente);
            evento.setMensagem(texto.trim());
            String jsonEvento = MAPPER.writeValueAsString(evento);

            if (isBroadcast(para)) {
                conexaoManager.enviarMensagemParaTodos(jsonEvento, remetente);
            } else {
                String destino = para.trim();
                if (!usuarioRepository.existeUsuario(destino)) {
                    return respostaErro("Destinatário não encontrado.");
                }
                if (!conexaoManager.usuarioEstaConectado(destino)) {
                    return respostaErro("Destinatário offline");
                }
                conexaoManager.enviarMensagemPrivada(destino, jsonEvento);
            }

            GenericResponse ok = new GenericResponse();
            ok.setResposta("200");
            ok.setMensagem("Mensagem enviada");
            return ok;
        } catch (Exception e) {
            return respostaErro("Falha ao enviar mensagem.");
        }
    }

    private static boolean isBroadcast(String para) {
        if (para == null) {
            return false;
        }
        String valor = para.trim();
        return DESTINO_TODOS.equals(valor) || "todos".equalsIgnoreCase(valor);
    }

    private static GenericResponse respostaErro(String mensagem) {
        GenericResponse r = new GenericResponse();
        r.setResposta("401");
        r.setMensagem(mensagem);
        r.setToken(null);
        return r;
    }
}
