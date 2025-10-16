package se.konstlotteriet.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "artworks")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_id", nullable = false)
    private Integer yearId;

    @NotBlank(message = "Priskategori är obligatorisk")
    @Column(name = "price_category", nullable = false)
    private String priceCategory;

    @NotBlank(message = "Titel är obligatorisk")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Konstnär är obligatorisk")
    @Column(nullable = false)
    private String artist;

    @Column
    private String technique;

    @Column
    private String size;

    @NotNull(message = "Inköpsår är obligatoriskt")
    @Column(name = "purchase_year", nullable = false)
    private Integer purchaseYear;

    @NotNull(message = "Lotteriår är obligatoriskt")
    @Column(name = "lottery_year", nullable = false)
    private Integer lotteryYear;

    @Column(name = "is_won")
    private Boolean isWon = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Artwork() {
    }

    public Artwork(String title, String artist, String priceCategory, Integer purchaseYear, Integer lotteryYear) {
        this.title = title;
        this.artist = artist;
        this.priceCategory = priceCategory;
        this.purchaseYear = purchaseYear;
        this.lotteryYear = lotteryYear;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYearId() {
        return yearId;
    }

    public void setYearId(Integer yearId) {
        this.yearId = yearId;
    }

    public String getPriceCategory() {
        return priceCategory;
    }

    public void setPriceCategory(String priceCategory) {
        this.priceCategory = priceCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getPurchaseYear() {
        return purchaseYear;
    }

    public void setPurchaseYear(Integer purchaseYear) {
        this.purchaseYear = purchaseYear;
    }

    public Integer getLotteryYear() {
        return lotteryYear;
    }

    public void setLotteryYear(Integer lotteryYear) {
        this.lotteryYear = lotteryYear;
    }

    public Boolean getIsWon() {
        return isWon;
    }

    public void setIsWon(Boolean won) {
        isWon = won;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}