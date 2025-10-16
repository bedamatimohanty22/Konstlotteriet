package se.konstlotteriet.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.konstlotteriet.domain.*;
import se.konstlotteriet.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LotteryService {

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    LotteryParticipantRepository participantRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    ArtworkRepository artworkRepository;

    public List<Lottery> getAllLotteries() {
        return lotteryRepository.listAll();
    }

    public Optional<Lottery> getLotteryById(Long id) {
        return Optional.ofNullable(lotteryRepository.findById(id));
    }

    public Optional<Lottery> getLotteryByYear(Integer year) {
        return lotteryRepository.findByYear(year);
    }

    public Optional<Lottery> getActiveLottery() {
        return lotteryRepository.findActiveLottery();
    }

    @Transactional
    public Lottery createLottery(Lottery lottery) {
        // Check if lottery for this year already exists
        if (lotteryRepository.findByYear(lottery.getYear()).isPresent()) {
            throw new IllegalArgumentException("En utlottning för år " + lottery.getYear() + " finns redan");
        }

        lottery.setStatus(LotteryStatus.PLANNED);
        lotteryRepository.persist(lottery);
        return lottery;
    }

    @Transactional
    public Lottery updateLottery(Long id, Lottery updatedLottery) {
        Lottery existingLottery = lotteryRepository.findById(id);
        if (existingLottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + id + " hittades inte");
        }

        existingLottery.setYear(updatedLottery.getYear());
        existingLottery.setLotteryDate(updatedLottery.getLotteryDate());
        existingLottery.setStatus(updatedLottery.getStatus());

        return existingLottery;
    }

    @Transactional
    public void deleteLottery(Long id) {
        Lottery lottery = lotteryRepository.findById(id);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + id + " hittades inte");
        }

        // Check if there are any participants
        long participantCount = participantRepository.countParticipants(lottery);
        if (participantCount > 0) {
            throw new IllegalStateException("Kan inte ta bort utlottning med deltagare. Ta bort deltagarna först.");
        }

        lotteryRepository.delete(lottery);
    }

    /**
     * Add members to lottery with correct number of tickets based on business rules:
     * - New member or no win last year: 3 tickets
     * - Won last year: 1 ticket
     * - Member can have max 3 tickets
     */
    @Transactional
    public void addMembersToLottery(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }

        List<Member> activeMembers = memberRepository.findActiveMembers();

        for (Member member : activeMembers) {
            // Check if member is already in this lottery
            if (participantRepository.findByLotteryAndMember(lottery, member).isEmpty()) {
                int numberOfTickets = calculateNumberOfTickets(member, lottery.getYear());

                LotteryParticipant participant = new LotteryParticipant(lottery, member, numberOfTickets);
                participantRepository.persist(participant);
            }
        }
    }

    /**
     * Calculate number of tickets based on business rules
     */
    private int calculateNumberOfTickets(Member member, int lotteryYear) {
        Integer lastWinYear = member.getLastWinYear();

        // If member won last year, they get 1 ticket
        if (lastWinYear != null && lastWinYear == (lotteryYear - 1)) {
            return 1;
        }

        // Otherwise, they get 3 tickets (max)
        return 3;
    }

    @Transactional
    public void addMemberToLottery(Long lotteryId, Long memberId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }

        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + memberId + " hittades inte");
        }

        if (!member.getActive()) {
            throw new IllegalArgumentException("Medlem är inte aktiv");
        }

        // Check if already participating
        if (participantRepository.findByLotteryAndMember(lottery, member).isPresent()) {
            throw new IllegalArgumentException("Medlem deltar redan i denna utlottning");
        }

        int numberOfTickets = calculateNumberOfTickets(member, lottery.getYear());
        LotteryParticipant participant = new LotteryParticipant(lottery, member, numberOfTickets);
        participantRepository.persist(participant);
    }

    public List<LotteryParticipant> getLotteryParticipants(Long lotteryId) {
        return participantRepository.findByLotteryId(lotteryId);
    }

    public List<LotteryParticipant> getWinners(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }
        return participantRepository.findWinners(lottery);
    }

    @Transactional
    public void activateLottery(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }
        lottery.setStatus(LotteryStatus.ACTIVE);
    }

    @Transactional
    public void completeLottery(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }
        lottery.setStatus(LotteryStatus.COMPLETED);
    }
}
