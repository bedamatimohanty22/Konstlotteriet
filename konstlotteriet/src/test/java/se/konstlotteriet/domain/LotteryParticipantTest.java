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

@DisplayName("LotteryParticipant Entity Tests")
class LotteryParticipantTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid lottery participant with 3 tickets")
    void testValidParticipant_ThreeTickets() {
        // Given
        LotteryParticipant participant = createParticipant(3);

        // When
        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        // Then
        assertTrue(violations.isEmpty(), "Valid participant with 3 tickets should have no violations");
    }

    @Test
    @DisplayName("Should create valid lottery participant with 1 ticket")
    void testValidParticipant_OneTicket() {
        // Given
        LotteryParticipant participant = createParticipant(1);

        // When
        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        // Then
        assertTrue(violations.isEmpty(), "Valid participant with 1 ticket should have no violations");
    }

    @Test
    @DisplayName("Should fail validation when number of tickets is 0")
    void testInvalidParticipant_ZeroTickets() {
        // Given
        LotteryParticipant participant = createParticipant(0);

        // When
        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        // Then
        assertFalse(violations.isEmpty(), "Participant with 0 tickets should have violations");
    }

    @Test
    @DisplayName("Should fail validation when number of tickets exceeds 3")
    void testInvalidParticipant_TooManyTickets() {
        // Given
        LotteryParticipant participant = createParticipant(4);

        // When
        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        // Then
        assertFalse(violations.isEmpty(), "Participant with more than 3 tickets should have violations");
    }

    @Test
    @DisplayName("Should fail validation when number of tickets is negative")
    void testInvalidParticipant_NegativeTickets() {
        // Given
        LotteryParticipant participant = createParticipant(-1);

        // When
        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        // Then
        assertFalse(violations.isEmpty(), "Participant with negative tickets should have violations");
    }

    @Test
    @DisplayName("Should track winner status")
    void testWinnerStatus() {
        // Given
        LotteryParticipant participant = createParticipant(3);
        assertFalse(participant.getHasWon(), "Initial winner status should be false");

        // When
        participant.setHasWon(true);

        // Then
        assertTrue(participant.getHasWon(), "Winner status should be updated");
    }

    @Test
    @DisplayName("Should associate with artwork when won")
    void testWonArtwork() {
        // Given
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Beautiful Painting");

        LotteryParticipant participant = createParticipant(3);
        assertNull(participant.getWonArtwork(), "Initial won artwork should be null");

        // When
        participant.setHasWon(true);
        participant.setWonArtwork(artwork);

        // Then
        assertTrue(participant.getHasWon());
        assertNotNull(participant.getWonArtwork());
        assertEquals(1L, participant.getWonArtwork().getId());
    }

    @Test
    @DisplayName("Should validate business rule: member won last year gets 1 ticket")
    void testBusinessRule_WonLastYear() {
        // This business rule is enforced in LotteryService, not the entity
        // But we can verify the entity can hold 1 ticket correctly
        LotteryParticipant participant = createParticipant(1);

        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        assertTrue(violations.isEmpty(), "Participant with 1 ticket (won last year) should be valid");
        assertEquals(1, participant.getNumberOfTickets());
    }

    @Test
    @DisplayName("Should validate business rule: regular member gets 3 tickets")
    void testBusinessRule_RegularMember() {
        // This business rule is enforced in LotteryService, not the entity
        // But we can verify the entity can hold 3 tickets correctly
        LotteryParticipant participant = createParticipant(3);

        Set<ConstraintViolation<LotteryParticipant>> violations = validator.validate(participant);

        assertTrue(violations.isEmpty(), "Participant with 3 tickets (regular) should be valid");
        assertEquals(3, participant.getNumberOfTickets());
    }

    // Helper method
    private LotteryParticipant createParticipant(int numberOfTickets) {
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Test");
        member.setLastName("Member");
        member.setEmail("test@test.se");

        Lottery lottery = new Lottery();
        lottery.setId(1L);
        lottery.setYear(2025);

        LotteryParticipant participant = new LotteryParticipant();
        participant.setMember(member);
        participant.setLottery(lottery);
        participant.setNumberOfTickets(numberOfTickets);
        participant.setHasWon(false);

        return participant;
    }
}
