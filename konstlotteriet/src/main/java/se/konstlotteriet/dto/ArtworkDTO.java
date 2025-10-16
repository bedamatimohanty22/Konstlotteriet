package se.konstlotteriet.dto;

import se.konstlotteriet.domain.Artwork;

public class ArtworkDTO {

    public Long id;
    public Integer yearId;
    public String priceCategory;
    public String title;
    public String artist;
    public String technique;
    public String size;
    public Integer purchaseYear;
    public Integer lotteryYear;
    public Boolean isWon;

    public ArtworkDTO() {
    }

    public static ArtworkDTO fromEntity(Artwork artwork) {
        ArtworkDTO dto = new ArtworkDTO();
        dto.id = artwork.getId();
        dto.yearId = artwork.getYearId();
        dto.priceCategory = artwork.getPriceCategory();
        dto.title = artwork.getTitle();
        dto.artist = artwork.getArtist();
        dto.technique = artwork.getTechnique();
        dto.size = artwork.getSize();
        dto.purchaseYear = artwork.getPurchaseYear();
        dto.lotteryYear = artwork.getLotteryYear();
        dto.isWon = artwork.getIsWon();
        return dto;
    }

    public Artwork toEntity() {
        Artwork artwork = new Artwork();
        artwork.setPriceCategory(this.priceCategory);
        artwork.setTitle(this.title);
        artwork.setArtist(this.artist);
        artwork.setTechnique(this.technique);
        artwork.setSize(this.size);
        artwork.setPurchaseYear(this.purchaseYear);
        artwork.setLotteryYear(this.lotteryYear);
        return artwork;
    }
}
