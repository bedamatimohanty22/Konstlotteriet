package se.konstlotteriet.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ranking_items")
public class RankingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranking_list_id", nullable = false)
    private RankingList rankingList;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    @NotNull
    @Column(name = "rank", nullable = false)
    private Integer rank;

    // Constructors
    public RankingItem() {
    }

    public RankingItem(RankingList rankingList, Artwork artwork, Integer rank) {
        this.rankingList = rankingList;
        this.artwork = artwork;
        this.rank = rank;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RankingList getRankingList() {
        return rankingList;
    }

    public void setRankingList(RankingList rankingList) {
        this.rankingList = rankingList;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
