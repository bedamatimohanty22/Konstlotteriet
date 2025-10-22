package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.Artwork;

import java.util.List;

@ApplicationScoped
public class ArtworkRepository implements PanacheRepository<Artwork> {

    public List<Artwork> findByLotteryYear(Integer year) {
        return list("lotteryYear", year);
    }

    public List<Artwork> findAvailableForLottery(Integer year) {
        return list("lotteryYear = ?1 and isWon = false", year);
    }

    public List<Artwork> findByLotteryYearAndNotWon(Integer year) {
        return list("lotteryYear = ?1 and isWon = false", year);
    }

    public Integer getNextYearId(Integer year) {
        Integer maxYearId = find("SELECT MAX(yearId) FROM Artwork WHERE lotteryYear = ?1", year)
                .project(Integer.class)
                .firstResult();
        return maxYearId != null ? maxYearId + 1 : 1;
    }

    public List<Artwork> findByArtist(String artist) {
        return list("artist", artist);
    }

    public List<Artwork> findWonArtworks() {
        return list("isWon", true);
    }
}
