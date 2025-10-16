package se.konstlotteriet.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import se.konstlotteriet.domain.User;

import java.util.Arrays;
import java.util.HashSet;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        var builder = Jwt.issuer(issuer)
                .upn(user.getEmail())
                .groups(new HashSet<>(Arrays.asList(user.getRole().name())))
                .claim("userId", user.getId());

        // Only add memberId claim if user has an associated member
        if (user.getMember() != null) {
            builder.claim("memberId", user.getMember().getId());
        }

        return builder.expiresIn(86400) // 24 hours
                .sign();
    }

    /**
     * Generate refresh token (longer expiration)
     */
    public String generateRefreshToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.getEmail())
                .claim("userId", user.getId())
                .claim("refresh", true)
                .expiresIn(604800) // 7 days
                .sign();
    }
}
