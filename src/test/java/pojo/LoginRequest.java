package pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginRequest(
        String username,
        String password,
        Integer expiresInMins) {
    public LoginRequest(String username, String password) {
        this(username, password, null);
    }
}