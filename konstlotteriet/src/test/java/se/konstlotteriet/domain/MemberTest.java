package se.konstlotteriet.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Member Entity Tests")
class MemberTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid member")
    void testValidMember() {
        // Given
        Member member = new Member();
        member.setFirstName("Anna");
        member.setLastName("Andersson");
        member.setEmail("anna@test.se");
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2024);
        member.setActive(true);

        // When
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Then
        assertTrue(violations.isEmpty(), "Valid member should have no violations");
    }

    @Test
    @DisplayName("Should fail validation when firstName is blank")
    void testInvalidMember_BlankFirstName() {
        // Given
        Member member = new Member();
        member.setFirstName("");  // Blank
        member.setLastName("Andersson");
        member.setEmail("anna@test.se");
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2024);

        // When
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Then
        assertFalse(violations.isEmpty(), "Member with blank firstName should have violations");
    }

    @Test
    @DisplayName("Should fail validation when email is invalid")
    void testInvalidMember_InvalidEmail() {
        // Given
        Member member = new Member();
        member.setFirstName("Anna");
        member.setLastName("Andersson");
        member.setEmail("not-an-email");  // Invalid email
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2024);

        // When
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Then
        assertFalse(violations.isEmpty(), "Member with invalid email should have violations");
    }

    @Test
    @DisplayName("Should set default active status to true")
    void testDefaultActiveStatus() {
        // Given & When
        Member member = new Member();

        // Then
        assertTrue(member.getActive(), "Default active status should be true");
    }

    @Test
    @DisplayName("Should track last win year")
    void testLastWinYear() {
        // Given
        Member member = new Member();
        assertNull(member.getLastWinYear(), "Initial last win year should be null");

        // When
        member.setLastWinYear(2024);

        // Then
        assertEquals(2024, member.getLastWinYear());
    }

    @Test
    @DisplayName("Should allow null last win year for members who never won")
    void testNullLastWinYear() {
        // Given
        Member member = new Member();
        member.setFirstName("Björn");
        member.setLastName("Bergström");
        member.setEmail("bjorn@test.se");
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2024);
        member.setLastWinYear(null);

        // When
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Then
        assertTrue(violations.isEmpty(), "Member with null lastWinYear should be valid");
    }

    @Test
    @DisplayName("Should deactivate member (soft delete)")
    void testDeactivateMember() {
        // Given
        Member member = new Member();
        member.setActive(true);

        // When
        member.setActive(false);

        // Then
        assertFalse(member.getActive(), "Member should be deactivated");
    }
}
