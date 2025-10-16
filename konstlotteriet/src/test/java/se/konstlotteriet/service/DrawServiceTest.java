package se.konstlotteriet.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.konstlotteriet.domain.*;
import se.konstlotteriet.repository.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DrawServiceTest {

    @Inject
    DrawService drawService;

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    LotteryParticipantRepository participantRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    ArtworkRepository artworkRepository;

    @Inject
    RankingListRepository rankingListRepository;

    @Inject
    RankingItemRepository rankingItemRepository;

    private Lottery lottery;
    private Member member1;
    private Member member2;
    private Member member3;
    private Artwork artwork1;
    private Artwork artwork2;
    private Artwork artwork3;

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up previous test data
        rankingItemRepository.deleteAll();
        rankingListRepository.deleteAll();
        participantRepository.deleteAll();
        artworkRepository.deleteAll();
        lotteryRepository.deleteAll();
        memberRepository.deleteAll();

        // Create lottery
        lottery = new Lottery();
        lottery.setYear(2024);
        lottery.setLotteryDate(LocalDate.now());
        lottery.setStatus(LotteryStatus.ACTIVE);
        lotteryRepository.persist(lottery);

        // Create members
        member1 = new Member("Anna", "Andersson", "anna@test.com");
        member1.setPhoneNumber("0701111111");
        member1.setMembershipStartYear(2020);
        member1.setActive(true);
        memberRepository.persist(member1);

        member2 = new Member("Bengt", "Bengtsson", "bengt@test.com");
        member2.setPhoneNumber("0702222222");
        member2.setMembershipStartYear(2021);
        member2.setActive(true);
        member2.setLastWinYear(2023); // Won last year, so will get 1 ticket
        memberRepository.persist(member2);

        member3 = new Member("Cecilia", "Carlsson", "cecilia@test.com");
        member3.setPhoneNumber("0703333333");
        member3.setMembershipStartYear(2022);
        member3.setActive(true);
        memberRepository.persist(member3);

        // Create artworks
        artwork1 = new Artwork();
        artwork1.setTitle("Konstverk 1");
        artwork1.setArtist("Konstnär 1");
        artwork1.setPriceCategory("A");
        artwork1.setTechnique("Olja");
        artwork1.setSize("50x70");
        artwork1.setPurchaseYear(2024);
        artwork1.setLotteryYear(2024);
        artwork1.setIsWon(false);
        artworkRepository.persist(artwork1);

        artwork2 = new Artwork();
        artwork2.setTitle("Konstverk 2");
        artwork2.setArtist("Konstnär 2");
        artwork2.setPriceCategory("B");
        artwork2.setTechnique("Akvarell");
        artwork2.setSize("40x60");
        artwork2.setPurchaseYear(2024);
        artwork2.setLotteryYear(2024);
        artwork2.setIsWon(false);
        artworkRepository.persist(artwork2);

        artwork3 = new Artwork();
        artwork3.setTitle("Konstverk 3");
        artwork3.setArtist("Konstnär 3");
        artwork3.setPriceCategory("C");
        artwork3.setTechnique("Skulptur");
        artwork3.setSize("30x30");
        artwork3.setPurchaseYear(2024);
        artwork3.setLotteryYear(2024);
        artwork3.setIsWon(false);
        artworkRepository.persist(artwork3);

        // Add participants with correct ticket counts
        LotteryParticipant p1 = new LotteryParticipant(lottery, member1, 3); // 3 tickets (default)
        LotteryParticipant p2 = new LotteryParticipant(lottery, member2, 1); // 1 ticket (won last year)
        LotteryParticipant p3 = new LotteryParticipant(lottery, member3, 3); // 3 tickets (default)
        participantRepository.persist(p1);
        participantRepository.persist(p2);
        participantRepository.persist(p3);
    }

    @Test
    @Transactional
    public void testPerformDraw_BasicScenario() {
        // When: Perform draw
        DrawService.DrawResult result = drawService.performDraw(lottery.getId());

        // Then: Verify results
        assertNotNull(result);
        assertEquals(2024, result.year);
        assertEquals(3, result.totalParticipants);
        assertEquals(3, result.totalArtworks);
        assertEquals(3, result.assignedArtworks);
        assertEquals(3, result.totalWinners);
        assertFalse(result.winners.isEmpty());

        // Verify lottery is completed
        Lottery updatedLottery = lotteryRepository.findById(lottery.getId());
        assertEquals(LotteryStatus.COMPLETED, updatedLottery.getStatus());

        // Verify all artworks are won
        List<Artwork> artworks = artworkRepository.findByLotteryYear(2024);
        assertTrue(artworks.stream().allMatch(Artwork::getIsWon));

        // Verify all participants are winners
        List<LotteryParticipant> participants = participantRepository.findByLottery(lottery);
        assertEquals(3, participants.stream().filter(LotteryParticipant::getHasWon).count());

        // Verify each winner has an artwork
        assertTrue(participants.stream().allMatch(p -> p.getHasWon() && p.getWonArtwork() != null));
    }

    @Test
    @Transactional
    public void testPerformDraw_WithRankingPreferences() {
        // Given: Create ranking lists with preferences
        // Member 1 prefers artwork1 > artwork2 > artwork3
        RankingList ranking1 = new RankingList(lottery, member1);
        rankingListRepository.persist(ranking1);
        rankingItemRepository.persist(new RankingItem(ranking1, artwork1, 1));
        rankingItemRepository.persist(new RankingItem(ranking1, artwork2, 2));
        rankingItemRepository.persist(new RankingItem(ranking1, artwork3, 3));

        // Member 2 prefers artwork2 > artwork1 > artwork3
        RankingList ranking2 = new RankingList(lottery, member2);
        rankingListRepository.persist(ranking2);
        rankingItemRepository.persist(new RankingItem(ranking2, artwork2, 1));
        rankingItemRepository.persist(new RankingItem(ranking2, artwork1, 2));
        rankingItemRepository.persist(new RankingItem(ranking2, artwork3, 3));

        // When: Perform draw
        DrawService.DrawResult result = drawService.performDraw(lottery.getId());

        // Then: Verify results
        assertEquals(3, result.totalWinners);
        assertEquals(3, result.assignedArtworks);

        // Verify preferences were considered (member1 has more tickets, so should have advantage)
        List<LotteryParticipant> participants = participantRepository.findByLottery(lottery);
        LotteryParticipant winner1 = participants.stream()
                .filter(p -> p.getMember().getId().equals(member1.getId()))
                .findFirst()
                .orElseThrow();

        assertTrue(winner1.getHasWon());
        assertNotNull(winner1.getWonArtwork());
    }

    @Test
    @Transactional
    public void testPerformDraw_MoreArtworksThanParticipants() {
        // Given: Add extra artwork
        Artwork artwork4 = new Artwork();
        artwork4.setTitle("Konstverk 4");
        artwork4.setArtist("Konstnär 4");
        artwork4.setPriceCategory("D");
        artwork4.setTechnique("Grafik");
        artwork4.setSize("20x30");
        artwork4.setPurchaseYear(2024);
        artwork4.setLotteryYear(2024);
        artwork4.setIsWon(false);
        artworkRepository.persist(artwork4);

        // When: Perform draw
        DrawService.DrawResult result = drawService.performDraw(lottery.getId());

        // Then: Only 3 winners (one per member)
        assertEquals(3, result.totalWinners);
        assertEquals(3, result.assignedArtworks);
        assertEquals(4, result.totalArtworks);

        // One artwork should remain unwon
        List<Artwork> artworks = artworkRepository.findByLotteryYear(2024);
        assertEquals(1, artworks.stream().filter(a -> !a.getIsWon()).count());
    }

    @Test
    @Transactional
    public void testPerformDraw_InvalidStatus() {
        // Given: Lottery is not active
        lottery.setStatus(LotteryStatus.PLANNED);
        lotteryRepository.persist(lottery);

        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            drawService.performDraw(lottery.getId());
        });
    }

    @Test
    @Transactional
    public void testPerformDraw_NoParticipants() {
        // Given: Remove all participants
        participantRepository.deleteAll();

        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            drawService.performDraw(lottery.getId());
        });
    }

    @Test
    @Transactional
    public void testPerformDraw_NoArtworks() {
        // Given: Remove all artworks
        artworkRepository.deleteAll();

        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            drawService.performDraw(lottery.getId());
        });
    }
}
