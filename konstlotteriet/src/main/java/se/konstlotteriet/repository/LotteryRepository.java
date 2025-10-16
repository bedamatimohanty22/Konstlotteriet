package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.LotteryStatus;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LotteryRepository implements PanacheRepository<Lottery> {

    public Optional<Lottery> findByYear(Integer year) {
        return find("year", year).firstResultOptional();
    }

    public List<Lottery> findByStatus(LotteryStatus status) {
        return list("status", status);
    }

    public Optional<Lottery> findActiveLottery() {
        return find("status", LotteryStatus.ACTIVE).firstResultOptional();
    }

    public List<Lottery> findCompletedLotteries() {
        return list("status = ?1 ORDER BY year DESC", LotteryStatus.COMPLETED);
    }
}
