package se.konstlotteriet.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.domain.Role;
import se.konstlotteriet.domain.User;
import se.konstlotteriet.repository.MemberRepository;
import se.konstlotteriet.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Inject
    AuthService authService;

    @Inject
    TokenService tokenService;

    UserRepository userRepository;
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        memberRepository = Mockito.mock(MemberRepository.class);

        QuarkusMock.installMockForType(userRepository, UserRepository.class);
        QuarkusMock.installMockForType(memberRepository, MemberRepository.class);
    }

    @Test
    @DisplayName("Should authenticate user with correct credentials")
    void testAuthenticate_Success() {
        // Given
        String email = "test@konstlotteriet.se";
        String password = "password123";
        String hashedPassword = BcryptUtil.bcryptHash(password);

        User user = createUser(1L, email, hashedPassword, Role.MEMBER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<String> token = authService.authenticate(email, password);

        // Then
        assertTrue(token.isPresent(), "Token should be present for valid credentials");
        assertNotNull(token.get(), "Token should not be null");
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should fail authentication with wrong password")
    void testAuthenticate_WrongPassword() {
        // Given
        String email = "test@konstlotteriet.se";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = BcryptUtil.bcryptHash(correctPassword);

        User user = createUser(1L, email, hashedPassword, Role.MEMBER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<String> token = authService.authenticate(email, wrongPassword);

        // Then
        assertFalse(token.isPresent(), "Token should not be present for wrong password");
    }

    @Test
    @DisplayName("Should fail authentication for non-existent user")
    void testAuthenticate_UserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        Optional<String> token = authService.authenticate("nonexistent@test.se", "password");

        // Then
        assertFalse(token.isPresent(), "Token should not be present for non-existent user");
    }

    @Test
    @DisplayName("Should fail authentication for inactive user")
    void testAuthenticate_InactiveUser() {
        // Given
        String email = "inactive@test.se";
        String password = "password123";
        String hashedPassword = BcryptUtil.bcryptHash(password);

        User user = createUser(1L, email, hashedPassword, Role.MEMBER);
        user.setActive(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        Optional<String> token = authService.authenticate(email, password);

        // Then
        assertFalse(token.isPresent(), "Token should not be present for inactive user");
    }

    @Test
    @DisplayName("Should register new admin user successfully")
    void testRegisterUser_AdminUser() {
        // Given
        String email = "admin@konstlotteriet.se";
        String password = "adminpass123";

        when(userRepository.emailExists(email)).thenReturn(false);
        doAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return null;
        }).when(userRepository).persist(any(User.class));

        // When
        User result = authService.registerUser(email, password, Role.ADMIN, null);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        assertNull(result.getMember(), "Admin user should not have associated member");
        assertTrue(result.getActive());
        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    @DisplayName("Should register new member user with member association")
    void testRegisterUser_MemberUser() {
        // Given
        Member member = createMember(5L, "Erik", "Eriksson");
        String email = "member@konstlotteriet.se";
        String password = "memberpass123";

        when(userRepository.emailExists(email)).thenReturn(false);
        when(memberRepository.findById(5L)).thenReturn(member);
        doAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return null;
        }).when(userRepository).persist(any(User.class));

        // When
        User result = authService.registerUser(email, password, Role.MEMBER, 5L);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Role.MEMBER, result.getRole());
        assertNotNull(result.getMember(), "Member user should have associated member");
        assertEquals(5L, result.getMember().getId());
        verify(memberRepository, times(1)).findById(5L);
    }

    @Test
    @DisplayName("Should throw exception when registering duplicate email")
    void testRegisterUser_DuplicateEmail() {
        // Given
        when(userRepository.emailExists("existing@test.se")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser("existing@test.se", "password123", Role.MEMBER, null);
        }, "Should throw exception for duplicate email");
    }

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        // Given
        String oldPassword = "oldpass123";
        String newPassword = "newpass456";
        String hashedOldPassword = BcryptUtil.bcryptHash(oldPassword);

        User user = createUser(1L, "user@test.se", hashedOldPassword, Role.MEMBER);
        when(userRepository.findById(1L)).thenReturn(user);

        // When
        authService.changePassword(1L, oldPassword, newPassword);

        // Then
        assertTrue(BcryptUtil.matches(newPassword, user.getPassword()),
                "Password should be updated to new password");
        assertFalse(BcryptUtil.matches(oldPassword, user.getPassword()),
                "Old password should no longer work");
    }

    @Test
    @DisplayName("Should fail password change with wrong old password")
    void testChangePassword_WrongOldPassword() {
        // Given
        String correctOldPassword = "oldpass123";
        String wrongOldPassword = "wrongpass";
        String hashedOldPassword = BcryptUtil.bcryptHash(correctOldPassword);

        User user = createUser(1L, "user@test.se", hashedOldPassword, Role.MEMBER);
        when(userRepository.findById(1L)).thenReturn(user);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.changePassword(1L, wrongOldPassword, "newpass456");
        }, "Should throw exception for wrong old password");
    }

    @Test
    @DisplayName("Should deactivate user")
    void testDeactivateUser() {
        // Given
        User user = createUser(1L, "test@test.se", "hashedpass", Role.MEMBER);
        when(userRepository.findById(1L)).thenReturn(user);

        // When
        authService.deactivateUser(1L);

        // Then
        assertFalse(user.getActive(), "User should be deactivated");
    }

    @Test
    @DisplayName("Should get user by email")
    void testGetUserByEmail() {
        // Given
        User user = createUser(1L, "test@test.se", "hashedpass", Role.MEMBER);
        when(userRepository.findByEmail("test@test.se")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = authService.getUserByEmail("test@test.se");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@test.se", result.get().getEmail());
    }

    // Helper methods
    private User createUser(Long id, String email, String hashedPassword, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Member createMember(Long id, String firstName, String lastName) {
        Member member = new Member();
        member.setId(id);
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(firstName.toLowerCase() + "@test.se");
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2020);
        member.setActive(true);
        return member;
    }
}
