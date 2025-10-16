package se.konstlotteriet.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.domain.Role;
import se.konstlotteriet.domain.User;
import se.konstlotteriet.repository.MemberRepository;
import se.konstlotteriet.repository.UserRepository;

import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    TokenService tokenService;

    /**
     * Authenticate user with email and password
     * Returns JWT token if successful
     */
    @Transactional
    public Optional<String> authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Check if user is active
        if (!user.getActive()) {
            System.out.println("DEBUG: User is not active");
            return Optional.empty();
        }

        // Verify password
        System.out.println("DEBUG: Checking password. Input: '" + password + "', Hash from DB: '" + user.getPassword() + "'");
        boolean passwordMatches = BcryptUtil.matches(password, user.getPassword());
        System.out.println("DEBUG: Password matches: " + passwordMatches);

        if (!passwordMatches) {
            return Optional.empty();
        }

        // Generate and return token
        String token = tokenService.generateToken(user);
        return Optional.of(token);
    }

    /**
     * Register new user (admin only should call this)
     */
    @Transactional
    public User registerUser(String email, String password, Role role, Long memberId) {
        // Check if email already exists
        if (userRepository.emailExists(email)) {
            throw new IllegalArgumentException("En användare med denna e-postadress finns redan");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(BcryptUtil.bcryptHash(password));
        user.setRole(role);
        user.setActive(true);

        // Link to member if provided
        if (memberId != null) {
            Member member = memberRepository.findById(memberId);
            if (member == null) {
                throw new IllegalArgumentException("Medlem med ID " + memberId + " hittades inte");
            }
            user.setMember(member);
        }

        userRepository.persist(user);
        return user;
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Användare hittades inte");
        }

        // Verify old password
        if (!BcryptUtil.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Felaktigt nuvarande lösenord");
        }

        // Update password
        user.setPassword(BcryptUtil.bcryptHash(newPassword));
    }

    /**
     * Reset password (admin function)
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Användare hittades inte");
        }

        user.setPassword(BcryptUtil.bcryptHash(newPassword));
    }

    /**
     * Deactivate user
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Användare hittades inte");
        }

        user.setActive(false);
    }

    /**
     * Reactivate user
     */
    @Transactional
    public void reactivateUser(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Användare hittades inte");
        }

        user.setActive(true);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get user by member ID
     */
    public Optional<User> getUserByMemberId(Long memberId) {
        return userRepository.findByMemberId(memberId);
    }
}
