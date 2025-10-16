package se.konstlotteriet.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    @NotBlank(message = "Nuvarande lösenord är obligatoriskt")
    public String oldPassword;

    @NotBlank(message = "Nytt lösenord är obligatoriskt")
    public String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
