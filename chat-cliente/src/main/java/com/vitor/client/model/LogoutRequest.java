package com.vitor.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoutRequest {

    @JsonProperty("op")
    private String op = "logout";

    @JsonProperty("token")
    private String token;
}
