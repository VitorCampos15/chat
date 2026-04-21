package com.vitor.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoutRequest {

    @JsonProperty("op")
    private String op;

    @JsonProperty("token")
    private String token;
}
