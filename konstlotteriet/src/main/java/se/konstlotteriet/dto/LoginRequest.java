package se.konstlotteriet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Email(message = "Ogiltig e-postadress")
    @NotBlank(message = "E-post är obligatorisk")
    public String email;

    @NotBlank(message = "Lösenord är obligatoriskt")
    public String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
