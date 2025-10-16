package se.konstlotteriet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @Email(message = "Ogiltig e-postadress")
    @NotBlank(message = "E-post är obligatorisk")
    public String email;

    @NotBlank(message = "Lösenord är obligatoriskt")
    public String password;

    @NotBlank(message = "Roll är obligatorisk")
    public String role;

    public Long memberId;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String role, Long memberId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.memberId = memberId;
    }
}
