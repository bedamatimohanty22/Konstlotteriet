package se.konstlotteriet.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import se.konstlotteriet.domain.Role;
import se.konstlotteriet.domain.User;
import se.konstlotteriet.dto.ChangePasswordRequest;
import se.konstlotteriet.dto.LoginRequest;
import se.konstlotteriet.dto.LoginResponse;
import se.konstlotteriet.dto.RegisterRequest;
import se.konstlotteriet.service.AuthService;

import java.util.Optional;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        Optional<String> tokenOpt = authService.authenticate(request.email, request.password);

        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Felaktiga inloggningsuppgifter"))
                    .build();
        }

        // Get user details for response
        Optional<User> userOpt = authService.getUserByEmail(request.email);
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        User user = userOpt.get();
        LoginResponse response = new LoginResponse(
                tokenOpt.get(),
                user.getEmail(),
                user.getRole().name(),
                user.getId(),
                user.getMember() != null ? user.getMember().getId() : null
        );

        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    @RolesAllowed("ADMIN")
    public Response register(@Valid RegisterRequest request) {
        try {
            Role role = Role.valueOf(request.role.toUpperCase());
            User user = authService.registerUser(request.email, request.password, role, request.memberId);

            return Response.status(Response.Status.CREATED)
                    .entity(new RegisterResponse(user.getId(), user.getEmail(), user.getRole().name()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/change-password")
    @RolesAllowed({"ADMIN", "MEMBER"})
    public Response changePassword(@Valid ChangePasswordRequest request, @Context SecurityContext ctx) {
        try {
            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            authService.changePassword(userId, request.oldPassword, request.newPassword);
            return Response.ok(new SuccessResponse("Lösenord ändrat")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "MEMBER"})
    public Response getCurrentUser(@Context SecurityContext ctx) {
        String email = jwt.getName();
        Optional<User> userOpt = authService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User user = userOpt.get();
        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getMember() != null ? user.getMember().getId() : null
        );

        return Response.ok(response).build();
    }

    // Response classes
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    public static class RegisterResponse {
        public Long userId;
        public String email;
        public String role;

        public RegisterResponse(Long userId, String email, String role) {
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }

    public static class UserInfoResponse {
        public Long userId;
        public String email;
        public String role;
        public Long memberId;

        public UserInfoResponse(Long userId, String email, String role, Long memberId) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.memberId = memberId;
        }
    }
}
