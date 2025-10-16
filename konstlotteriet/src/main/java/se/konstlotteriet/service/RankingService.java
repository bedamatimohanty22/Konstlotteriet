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
public class RankingService {

    @Inject
    RankingListRepository rankingListRepository;

    @Inject
    RankingItemRepository rankingItemRepository;

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    ArtworkRepository artworkRepository;

    @Transactional
    public RankingList createRankingList(Long lotteryId, Long memberId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }

        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + memberId + " hittades inte");
        }

        // Check if ranking list already exists
        Optional<RankingList> existing = rankingListRepository.findByLotteryAndMember(lottery, member);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Rankinglista finns redan för denna medlem och utlottning");
        }

        RankingList rankingList = new RankingList(lottery, member);
        rankingListRepository.persist(rankingList);
        return rankingList;
    }

    @Transactional
    public RankingList updateRankingList(Long rankingListId, List<RankingItemData> rankings) {
        RankingList rankingList = rankingListRepository.findById(rankingListId);
        if (rankingList == null) {
            throw new IllegalArgumentException("Rankinglista med ID " + rankingListId + " hittades inte");
        }

        // Delete existing ranking items
        rankingItemRepository.deleteByRankingList(rankingList);

        // Add new ranking items
        for (RankingItemData data : rankings) {
            Artwork artwork = artworkRepository.findById(data.artworkId);
            if (artwork == null) {
                throw new IllegalArgumentException("Konstverk med ID " + data.artworkId + " hittades inte");
            }

            RankingItem item = new RankingItem(rankingList, artwork, data.rank);
            rankingItemRepository.persist(item);
        }

        return rankingList;
    }

    @Transactional
    public void submitRankingList(Long rankingListId) {
        RankingList rankingList = rankingListRepository.findById(rankingListId);
        if (rankingList == null) {
            throw new IllegalArgumentException("Rankinglista med ID " + rankingListId + " hittades inte");
        }

        if (rankingList.getRankingItems().isEmpty()) {
            throw new IllegalArgumentException("Rankinglistan måste innehålla minst ett konstverk");
        }

        rankingList.setSubmittedAt(LocalDateTime.now());
    }

    public Optional<RankingList> getRankingListByLotteryAndMember(Long lotteryId, Long memberId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        Member member = memberRepository.findById(memberId);

        if (lottery == null || member == null) {
            return Optional.empty();
        }

        return rankingListRepository.findByLotteryAndMember(lottery, member);
    }

    public List<RankingList> getRankingListsByLottery(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Utlottning med ID " + lotteryId + " hittades inte");
        }
        return rankingListRepository.findByLottery(lottery);
    }

    @Transactional
    public void deleteRankingList(Long rankingListId) {
        RankingList rankingList = rankingListRepository.findById(rankingListId);
        if (rankingList == null) {
            throw new IllegalArgumentException("Rankinglista med ID " + rankingListId + " hittades inte");
        }
        rankingListRepository.delete(rankingList);
    }

    // Helper class for ranking item data
    public static class RankingItemData {
        public Long artworkId;
        public Integer rank;

        public RankingItemData() {}

        public RankingItemData(Long artworkId, Integer rank) {
            this.artworkId = artworkId;
            this.rank = rank;
        }
    }
}
