package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
        int id,
        String username,
        String email,
        String firstName,
        String lastName,
        String gender,
        String image,
        String accessToken,
        String refreshToken
) {}