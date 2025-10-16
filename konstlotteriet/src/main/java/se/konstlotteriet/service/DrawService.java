package se.konstlotteriet.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.konstlotteriet.domain.*;
import se.konstlotteriet.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class DrawService {

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    LotteryParticipantRepository participantRepository;

    @Inject
    ArtworkRepository artworkRepository;

    @Inject
    RankingListRepository rankingListRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    EmailService emailService;

    /**
     * Performs the lottery draw for a given lottery.
     * Algorithm:
     * 1. Get all participants with their ticket counts
     * 2. Get all available artworks
     * 3. For each artwork, select a winner based on:
     *    - Participant ranking preferences (if they submitted a list)
     *    - Number of tickets (weight in random selection)
     *    - Random selection if no preferences
     * 4. Update winners and mark artworks as won
     * 5. Update member's lastWinYear
     */
    @Transactional
    public DrawResult performDraw(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }

        if (lottery.getStatus() != LotteryStatus.ACTIVE) {
            throw new IllegalArgumentException("Endast aktiva utlottningar kan genomföras. Status: " + lottery.getStatus());
        }

        // Get all participants for this lottery
        List<LotteryParticipant> participants = participantRepository.findByLottery(lottery);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Inga deltagare i utlottningen");
        }

        // Get all available artworks for this lottery year
        List<Artwork> availableArtworks = artworkRepository.findByLotteryYearAndNotWon(lottery.getYear());
        if (availableArtworks.isEmpty()) {
            throw new IllegalArgumentException("Inga tillgängliga konstverk för utlottningen");
        }

        // Load ranking lists for all participants
        Map<Long, RankingList> rankingLists = new HashMap<>();
        for (LotteryParticipant participant : participants) {
            Optional<RankingList> rankingOpt = rankingListRepository
                    .findByLotteryAndMember(lottery, participant.getMember());
            rankingOpt.ifPresent(ranking -> rankingLists.put(participant.getMember().getId(), ranking));
        }

        // Track which members have already won
        Set<Long> winnersSet = new HashSet<>();
        List<DrawResult.WinnerInfo> winners = new ArrayList<>();
        int totalArtworks = availableArtworks.size();
        int assignedArtworks = 0;

        // Process each artwork
        for (Artwork artwork : availableArtworks) {
            // Get eligible participants (those who haven't won yet)
            List<LotteryParticipant> eligibleParticipants = participants.stream()
                    .filter(p -> !winnersSet.contains(p.getMember().getId()))
                    .collect(Collectors.toList());

            if (eligibleParticipants.isEmpty()) {
                // No more eligible participants
                break;
            }

            // Select winner for this artwork
            LotteryParticipant winner = selectWinner(artwork, eligibleParticipants, rankingLists);

            if (winner != null) {
                // Mark participant as winner
                winner.setHasWon(true);
                winner.setWonArtwork(artwork);
                participantRepository.persist(winner);

                // Mark artwork as won
                artwork.setIsWon(true);
                artworkRepository.persist(artwork);

                // Update member's last win year
                Member member = winner.getMember();
                member.setLastWinYear(lottery.getYear());
                memberRepository.persist(member);

                // Track winner
                winnersSet.add(member.getId());
                winners.add(new DrawResult.WinnerInfo(
                        member.getId(),
                        member.getFirstName() + " " + member.getLastName(),
                        artwork.getId(),
                        artwork.getTitle(),
                        artwork.getArtist()
                ));

                assignedArtworks++;
            }
        }

        // Mark lottery as completed
        lottery.setStatus(LotteryStatus.COMPLETED);
        lotteryRepository.persist(lottery);

        // Send email notifications to all participants
        try {
            emailService.sendDrawCompletionNotification(lottery, participants);
        } catch (Exception e) {
            // Log but don't fail the draw if emails fail
            System.err.println("Failed to send email notifications: " + e.getMessage());
        }

        return new DrawResult(
                lottery.getId(),
                lottery.getYear(),
                participants.size(),
                winners.size(),
                totalArtworks,
                assignedArtworks,
                winners
        );
    }

    /**
     * Selects a winner for a specific artwork based on ranking preferences and ticket weights.
     */
    private LotteryParticipant selectWinner(
            Artwork artwork,
            List<LotteryParticipant> eligibleParticipants,
            Map<Long, RankingList> rankingLists
    ) {
        // Find participants who have this artwork in their ranking list
        List<ParticipantWithPreference> participantsWithPreference = new ArrayList<>();

        for (LotteryParticipant participant : eligibleParticipants) {
            Long memberId = participant.getMember().getId();
            RankingList rankingList = rankingLists.get(memberId);

            int preferenceRank = Integer.MAX_VALUE; // No preference by default

            if (rankingList != null && rankingList.getRankingItems() != null) {
                // Check if this artwork is in the member's ranking list
                Optional<RankingItem> rankingItem = rankingList.getRankingItems().stream()
                        .filter(item -> item.getArtwork().getId().equals(artwork.getId()))
                        .findFirst();

                if (rankingItem.isPresent()) {
                    preferenceRank = rankingItem.get().getRank();
                }
            }

            participantsWithPreference.add(new ParticipantWithPreference(
                    participant,
                    preferenceRank,
                    participant.getNumberOfTickets()
            ));
        }

        // Sort by preference rank (lower is better), then by random
        participantsWithPreference.sort((a, b) -> {
            int rankComparison = Integer.compare(a.preferenceRank, b.preferenceRank);
            if (rankComparison != 0) {
                return rankComparison;
            }
            // If same rank (or both no preference), use weighted random based on tickets
            return 0; // Will use weighted random selection below
        });

        // Get the best preference rank
        int bestRank = participantsWithPreference.get(0).preferenceRank;

        // Get all participants with the best rank
        List<ParticipantWithPreference> bestCandidates = participantsWithPreference.stream()
                .filter(p -> p.preferenceRank == bestRank)
                .collect(Collectors.toList());

        // If multiple candidates with same preference, do weighted random selection
        if (bestCandidates.size() == 1) {
            return bestCandidates.get(0).participant;
        }

        // Weighted random selection based on number of tickets
        return weightedRandomSelection(bestCandidates);
    }

    /**
     * Performs weighted random selection based on number of tickets.
     * Members with more tickets have higher chance of winning.
     */
    private LotteryParticipant weightedRandomSelection(List<ParticipantWithPreference> candidates) {
        // Calculate total weight (sum of all tickets)
        int totalWeight = candidates.stream()
                .mapToInt(p -> p.numberOfTickets)
                .sum();

        // Generate random number between 0 and totalWeight
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight);

        // Select based on weighted probability
        int cumulativeWeight = 0;
        for (ParticipantWithPreference candidate : candidates) {
            cumulativeWeight += candidate.numberOfTickets;
            if (randomValue < cumulativeWeight) {
                return candidate.participant;
            }
        }

        // Fallback (should never happen)
        return candidates.get(0).participant;
    }

    /**
     * Helper class to track participant with their preference and tickets.
     */
    private static class ParticipantWithPreference {
        LotteryParticipant participant;
        int preferenceRank;
        int numberOfTickets;

        ParticipantWithPreference(LotteryParticipant participant, int preferenceRank, int numberOfTickets) {
            this.participant = participant;
            this.preferenceRank = preferenceRank;
            this.numberOfTickets = numberOfTickets;
        }
    }

    /**
     * Result of a lottery draw.
     */
    public static class DrawResult {
        public Long lotteryId;
        public Integer year;
        public int totalParticipants;
        public int totalWinners;
        public int totalArtworks;
        public int assignedArtworks;
        public List<WinnerInfo> winners;

        public DrawResult(Long lotteryId, Integer year, int totalParticipants, int totalWinners,
                          int totalArtworks, int assignedArtworks, List<WinnerInfo> winners) {
            this.lotteryId = lotteryId;
            this.year = year;
            this.totalParticipants = totalParticipants;
            this.totalWinners = totalWinners;
            this.totalArtworks = totalArtworks;
            this.assignedArtworks = assignedArtworks;
            this.winners = winners;
        }

        public static class WinnerInfo {
            public Long memberId;
            public String memberName;
            public Long artworkId;
            public String artworkTitle;
            public String artworkArtist;

            public WinnerInfo(Long memberId, String memberName, Long artworkId,
                              String artworkTitle, String artworkArtist) {
                this.memberId = memberId;
                this.memberName = memberName;
                this.artworkId = artworkId;
                this.artworkTitle = artworkTitle;
                this.artworkArtist = artworkArtist;
            }
        }
    }
}
