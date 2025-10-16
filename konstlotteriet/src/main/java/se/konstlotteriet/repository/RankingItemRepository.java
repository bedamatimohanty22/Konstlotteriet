package se.konstlotteriet.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.konstlotteriet.domain.RankingItem;
import se.konstlotteriet.domain.RankingList;

import java.util.List;

@ApplicationScoped
public class RankingItemRepository implements PanacheRepository<RankingItem> {

    public List<RankingItem> findByRankingList(RankingList rankingList) {
        return list("rankingList = ?1 ORDER BY rank ASC", rankingList);
    }

    public void deleteByRankingList(RankingList rankingList) {
        delete("rankingList", rankingList);
    }
}
