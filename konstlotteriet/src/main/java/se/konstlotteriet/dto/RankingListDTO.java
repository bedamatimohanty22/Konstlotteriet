package se.konstlotteriet.dto;

import se.konstlotteriet.domain.RankingItem;
import se.konstlotteriet.domain.RankingList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RankingListDTO {

    public Long id;
    public Long lotteryId;
    public MemberDTO member;
    public List<RankingItemDTO> rankings;
    public LocalDateTime submittedAt;

    public RankingListDTO() {
    }

    public static RankingListDTO fromEntity(RankingList rankingList) {
        RankingListDTO dto = new RankingListDTO();
        dto.id = rankingList.getId();
        dto.lotteryId = rankingList.getLottery().getId();
        dto.member = MemberDTO.fromEntity(rankingList.getMember());
        dto.submittedAt = rankingList.getSubmittedAt();

        dto.rankings = rankingList.getRankingItems().stream()
                .map(RankingItemDTO::fromEntity)
                .collect(Collectors.toList());

        return dto;
    }

    public static class RankingItemDTO {
        public Long id;
        public ArtworkDTO artwork;
        public Integer rank;

        public RankingItemDTO() {
        }

        public static RankingItemDTO fromEntity(RankingItem item) {
            RankingItemDTO dto = new RankingItemDTO();
            dto.id = item.getId();
            dto.artwork = ArtworkDTO.fromEntity(item.getArtwork());
            dto.rank = item.getRank();
            return dto;
        }
    }
}
