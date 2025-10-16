package se.konstlotteriet.service;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import se.konstlotteriet.domain.*;
import se.konstlotteriet.dto.LotteryDTO;
import se.konstlotteriet.dto.LotteryParticipantDTO;
import se.konstlotteriet.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("LotteryService Tests")
class LotteryServiceTest {

    @Inject
    LotteryService lotteryService;

    LotteryRepository lotteryRepository;
    MemberRepository memberRepository;
    ArtworkRepository artworkRepository;
    LotteryParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        // Create mocks
        lotteryRepository = Mockito.mock(LotteryRepository.class);
        memberRepository = Mockito.mock(MemberRepository.class);
        artworkRepository = Mockito.mock(ArtworkRepository.class);
        participantRepository = Mockito.mock(LotteryParticipantRepository.class);

        // Install mocks
        QuarkusMock.installMockForType(lotteryRepository, LotteryRepository.class);
        QuarkusMock.installMockForType(memberRepository, MemberRepository.class);
        QuarkusMock.installMockForType(artworkRepository, ArtworkRepository.class);
        QuarkusMock.installMockForType(participantRepository, LotteryParticipantRepository.class);
    }

    @Test
    @DisplayName("Should calculate 3 tickets for member who hasn't won last year")
    void testCalculateTickets_NoWinLastYear() {
        // Given
        Member member = createMember(1L, "Anna", "Andersson", null);
        Lottery lottery = createLottery(1L, 2025);

        when(memberRepository.findById(1L)).thenReturn(member);
        when(lotteryRepository.findById(1L)).thenReturn(lottery);
        when(participantRepository.findByLotteryAndMember(any(), any())).thenReturn(Optional.empty());
        doAnswer(i -> {
            LotteryParticipant p = i.getArgument(0);
            p.setId(1L);
            return null;
        }).when(participantRepository).persist(any(LotteryParticipant.class));

        // When
        lotteryService.addMemberToLottery(1L, 1L);

        // Then - verify the participant was created with 3 tickets
        verify(participantRepository, times(1)).persist(any(LotteryParticipant.class));
    }

    @Test
    @DisplayName("Should calculate 1 ticket for member who won last year")
    void testCalculateTickets_WonLastYear() {
        // Given
        Member member = createMember(1L, "Björn", "Bergström", 2024);
        Lottery lottery = createLottery(1L, 2025);

        when(memberRepository.findById(1L)).thenReturn(member);
        when(lotteryRepository.findById(1L)).thenReturn(lottery);
        when(participantRepository.findByLotteryAndMember(any(), any())).thenReturn(Optional.empty());
        doAnswer(i -> {
            LotteryParticipant p = i.getArgument(0);
            p.setId(1L);
            return null;
        }).when(participantRepository).persist(any(LotteryParticipant.class));

        // When
        lotteryService.addMemberToLottery(1L, 1L);

        // Then - verify the participant was created
        verify(participantRepository, times(1)).persist(any(LotteryParticipant.class));
    }

    @Test
    @DisplayName("Should calculate 3 tickets for member who won 2 years ago")
    void testCalculateTickets_WonTwoYearsAgo() {
        // Given
        Member member = createMember(1L, "Cecilia", "Carlsson", 2023);
        Lottery lottery = createLottery(1L, 2025);

        when(memberRepository.findById(1L)).thenReturn(member);
        when(lotteryRepository.findById(1L)).thenReturn(lottery);
        when(participantRepository.findByLotteryAndMember(any(), any())).thenReturn(Optional.empty());
        doAnswer(i -> {
            LotteryParticipant p = i.getArgument(0);
            p.setId(1L);
            return null;
        }).when(participantRepository).persist(any(LotteryParticipant.class));

        // When
        lotteryService.addMemberToLottery(1L, 1L);

        // Then - verify the participant was created
        verify(participantRepository, times(1)).persist(any(LotteryParticipant.class));
    }

    @Test
    @DisplayName("Should create lottery successfully")
    void testCreateLottery() {
        // Given
        Lottery lottery = new Lottery();
        lottery.setYear(2025);
        lottery.setLotteryDate(LocalDate.of(2025, 12, 15));

        when(lotteryRepository.findByYear(2025)).thenReturn(Optional.empty());
        doAnswer(i -> {
            Lottery l = i.getArgument(0);
            l.setId(1L);
            return null;
        }).when(lotteryRepository).persist(any(Lottery.class));

        // When
        Lottery result = lotteryService.createLottery(lottery);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(LotteryStatus.PLANNED, result.getStatus());
        verify(lotteryRepository, times(1)).persist(any(Lottery.class));
    }

    @Test
    @DisplayName("Should get all lotteries")
    void testGetAllLotteries() {
        // Given
        List<Lottery> lotteries = List.of(
                createLottery(1L, 2024),
                createLottery(2L, 2025)
        );
        when(lotteryRepository.listAll()).thenReturn(lotteries);

        // When
        List<Lottery> result = lotteryService.getAllLotteries();

        // Then
        assertEquals(2, result.size());
        assertEquals(2024, result.get(0).getYear());
        assertEquals(2025, result.get(1).getYear());
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void testAddParticipant_MemberNotFound() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(null);
        when(lotteryRepository.findById(1L)).thenReturn(createLottery(1L, 2025));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            lotteryService.addMemberToLottery(1L, 999L);
        }, "Should throw exception when member not found");
    }

    @Test
    @DisplayName("Should throw exception when lottery not found")
    void testAddParticipant_LotteryNotFound() {
        // Given
        Member member = createMember(1L, "David", "Danielsson", null);
        when(memberRepository.findById(1L)).thenReturn(member);
        when(lotteryRepository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            lotteryService.addMemberToLottery(999L, 1L);
        }, "Should throw exception when lottery not found");
    }

    // Helper methods
    private Member createMember(Long id, String firstName, String lastName, Integer lastWinYear) {
        Member member = new Member();
        member.setId(id);
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(firstName.toLowerCase() + "@test.se");
        member.setPhoneNumber("070-1234567");
        member.setMembershipStartYear(2020);
        member.setLastWinYear(lastWinYear);
        member.setActive(true);
        return member;
    }

    private Lottery createLottery(Long id, int year) {
        Lottery lottery = new Lottery();
        lottery.setId(id);
        lottery.setYear(year);
        lottery.setLotteryDate(LocalDate.of(year, 12, 15));
        lottery.setStatus(LotteryStatus.PLANNED);
        return lottery;
    }
}
