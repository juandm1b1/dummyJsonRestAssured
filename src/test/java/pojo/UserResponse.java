package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserResponse(
        int id,
        String firstName,
        String lastName,
        String maidenName,
        int age,
        String gender,
        String email,
        String phone,
        String username,
        String password,
        String birthDate,
        String image,
        String bloodGroup,
        double height,
        double weight,
        String eyeColor,
        Hair hair,
        String ip,
        Address address,
        String macAddress,
        String university,
        Bank bank,
        Company company,
        String ein,
        String ssn,
        String userAgent,
        Crypto crypto,
        String role
) {
    // Sub-record "hair"
    public record Hair(String color, String type) {}

    // Sub-record "address"
    public record Address(
            String address,
            String city,
            String state,
            String stateCode,
            String postalCode,
            Coordinates coordinates,
            String country
    ) {}

    // Sub-record "coordinates" (inside Address)
    public record Coordinates(double lat, double lng) {}

    // Sub-record "bank"
    public record Bank(
            String cardExpire,
            String cardNumber,
            String cardType,
            String currency,
            String iban
    ) {}

    // Sub-record "company"
    public record Company(
            String department,
            String name,
            String title,
            Address address
    ) {}

    // Sub-record "crypto"
    public record Crypto(String coin, String wallet, String network) {}
}