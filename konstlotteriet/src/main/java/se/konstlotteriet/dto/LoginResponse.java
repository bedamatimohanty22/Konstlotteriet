package se.konstlotteriet.dto;

public class LoginResponse {

    public String token;
    public String email;
    public String role;
    public Long userId;
    public Long memberId;

    public LoginResponse() {
    }

    public LoginResponse(String token, String email, String role, Long userId, Long memberId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.memberId = memberId;
    }
}
