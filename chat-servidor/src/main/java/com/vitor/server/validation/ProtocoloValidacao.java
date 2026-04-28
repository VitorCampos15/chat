package com.vitor.server.validation;

import java.util.regex.Pattern;

public final class ProtocoloValidacao {

    private ProtocoloValidacao() {
    }

    public static final String MSG_USUARIO_FORMATO =
            "Usuário com nome inválido (espaços, caracteres especiais ou nome com menos ou mais caracteres aceitaveis [5 à 20]).";

    public static final String MSG_SENHA_FORMATO =
            "Senha inválida (a senha deve conter exatamente 6 dígitos numéricos).";

    /** Letras e números apenas, comprimento 5–20 (sem espaços nem caracteres especiais). */
    private static final Pattern USUARIO_ALFANUMERICO = Pattern.compile("^[a-zA-Z0-9]{5,20}$");

    private static final Pattern SENHA_SEIS_DIGITOS = Pattern.compile("^\\d{6}$");

    public static boolean isUsuarioConforme(String usuario) {
        return usuario != null && USUARIO_ALFANUMERICO.matcher(usuario).matches();
    }

    public static boolean isSenhaConforme(String senha) {
        return senha != null && SENHA_SEIS_DIGITOS.matcher(senha).matches();
    }
}
