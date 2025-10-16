package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.LotteryParticipant;
import se.konstlotteriet.domain.Member;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LotteryParticipantRepository implements PanacheRepository<LotteryParticipant> {

    public List<LotteryParticipant> findByLottery(Lottery lottery) {
        return list("lottery", lottery);
    }

    public List<LotteryParticipant> findByLotteryId(Long lotteryId) {
        return list("lottery.id", lotteryId);
    }

    public Optional<LotteryParticipant> findByLotteryAndMember(Lottery lottery, Member member) {
        return find("lottery = ?1 and member = ?2", lottery, member).firstResultOptional();
    }

    public List<LotteryParticipant> findWinners(Lottery lottery) {
        return list("lottery = ?1 and hasWon = true", lottery);
    }

    public long countParticipants(Lottery lottery) {
        return count("lottery", lottery);
    }

    public long countTotalTickets(Lottery lottery) {
        Long total = find("SELECT SUM(numberOfTickets) FROM LotteryParticipant WHERE lottery = ?1", lottery)
                .project(Long.class)
                .firstResult();
        return total != null ? total : 0L;
    }
}
