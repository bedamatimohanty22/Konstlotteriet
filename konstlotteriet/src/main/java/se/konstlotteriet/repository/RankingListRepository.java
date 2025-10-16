package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.domain.RankingList;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RankingListRepository implements PanacheRepository<RankingList> {

    public Optional<RankingList> findByLotteryAndMember(Lottery lottery, Member member) {
        return find("lottery = ?1 and member = ?2", lottery, member).firstResultOptional();
    }

    public List<RankingList> findByLottery(Lottery lottery) {
        return list("lottery", lottery);
    }

    public List<RankingList> findSubmittedByLottery(Lottery lottery) {
        return find("lottery = ?1 and submittedAt is not null", lottery).list();
    }

    public long countSubmittedRankings(Lottery lottery) {
        return count("lottery = ?1 and submittedAt is not null", lottery);
    }
}
