package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.Member;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MemberRepository implements PanacheRepository<Member> {

    public Optional<Member> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<Member> findActiveMembers() {
        return list("active", true);
    }

    public List<Member> findByLastWinYear(Integer year) {
        return list("lastWinYear", year);
    }

    public long countActiveMembers() {
        return count("active", true);
    }
}
