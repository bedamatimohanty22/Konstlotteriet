package se.konstlotteriet.domain;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid user")
    void testValidUser() {
        // Given
        User user = new User();
        user.setEmail("test@konstlotteriet.se");
        user.setPassword(BcryptUtil.bcryptHash("password123"));
        user.setRole(Role.MEMBER);
        user.setActive(true);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void testInvalidUser_BlankEmail() {
        // Given
        User user = new User();
        user.setEmail("");  // Blank
        user.setPassword(BcryptUtil.bcryptHash("password123"));
        user.setRole(Role.MEMBER);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty(), "User with blank email should have violations");
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void testInvalidUser_InvalidEmailFormat() {
        // Given
        User user = new User();
        user.setEmail("not-an-email");  // Invalid format
        user.setPassword(BcryptUtil.bcryptHash("password123"));
        user.setRole(Role.MEMBER);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertFalse(violations.isEmpty(), "User with invalid email should have violations");
    }

    @Test
    @DisplayName("Should return role name from getRoles method")
    void testGetRoles() {
        // Given
        User user = new User();
        user.setRole(Role.ADMIN);

        // When
        String roles = user.getRoles();

        // Then
        assertEquals("ADMIN", roles, "getRoles should return role name");
    }

    @Test
    @DisplayName("Should return null from getRoles when role is null")
    void testGetRoles_NullRole() {
        // Given
        User user = new User();
        user.setRole(null);

        // When
        String roles = user.getRoles();

        // Then
        assertNull(roles, "getRoles should return null when role is null");
    }

    @Test
    @DisplayName("Should hash password with BCrypt")
    void testPasswordHashing() {
        // Given
        String plainPassword = "mySecretPassword123";
        String hashedPassword = BcryptUtil.bcryptHash(plainPassword);

        User user = new User();
        user.setPassword(hashedPassword);

        // Then
        assertNotEquals(plainPassword, user.getPassword(), "Password should be hashed");
        assertTrue(BcryptUtil.matches(plainPassword, user.getPassword()),
                "Hashed password should match original");
    }

    @Test
    @DisplayName("Should set default active status to true")
    void testDefaultActiveStatus() {
        // Given & When
        User user = new User();

        // Then
        assertTrue(user.getActive(), "Default active status should be true");
    }

    @Test
    @DisplayName("Should create admin user without member association")
    void testAdminUser_NoMember() {
        // Given
        User user = new User();
        user.setEmail("admin@konstlotteriet.se");
        user.setPassword(BcryptUtil.bcryptHash("adminpass"));
        user.setRole(Role.ADMIN);
        user.setMember(null);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty(), "Admin user without member should be valid");
        assertNull(user.getMember(), "Admin should not have member association");
    }

    @Test
    @DisplayName("Should create member user with member association")
    void testMemberUser_WithMember() {
        // Given
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Test");
        member.setLastName("User");
        member.setEmail("test@test.se");

        User user = new User();
        user.setEmail("member@konstlotteriet.se");
        user.setPassword(BcryptUtil.bcryptHash("memberpass"));
        user.setRole(Role.MEMBER);
        user.setMember(member);

        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertTrue(violations.isEmpty(), "Member user with member should be valid");
        assertNotNull(user.getMember(), "Member user should have member association");
        assertEquals(1L, user.getMember().getId());
    }

    @Test
    @DisplayName("Should deactivate user")
    void testDeactivateUser() {
        // Given
        User user = new User();
        user.setActive(true);

        // When
        user.setActive(false);

        // Then
        assertFalse(user.getActive(), "User should be deactivated");
    }
}
