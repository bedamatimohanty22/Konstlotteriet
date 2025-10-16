package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.User;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByMemberId(Long memberId) {
        return find("member.id", memberId).firstResultOptional();
    }

    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }
}
