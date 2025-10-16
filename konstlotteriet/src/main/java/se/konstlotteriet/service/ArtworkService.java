package se.konstlotteriet.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.konstlotteriet.domain.Artwork;
import se.konstlotteriet.repository.ArtworkRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ArtworkService {

    @Inject
    ArtworkRepository artworkRepository;

    public List<Artwork> getAllArtworks() {
        return artworkRepository.listAll();
    }

    public Optional<Artwork> getArtworkById(Long id) {
        return Optional.ofNullable(artworkRepository.findById(id));
    }

    public List<Artwork> getArtworksByYear(Integer year) {
        return artworkRepository.findByLotteryYear(year);
    }

    public List<Artwork> getAvailableArtworksForYear(Integer year) {
        return artworkRepository.findAvailableForLottery(year);
    }

    @Transactional
    public Artwork createArtwork(Artwork artwork) {
        // Auto-generate yearId
        Integer nextYearId = artworkRepository.getNextYearId(artwork.getLotteryYear());
        artwork.setYearId(nextYearId);
        artwork.setIsWon(false);

        artworkRepository.persist(artwork);
        return artwork;
    }

    @Transactional
    public Artwork updateArtwork(Long id, Artwork updatedArtwork) {
        Artwork existingArtwork = artworkRepository.findById(id);
        if (existingArtwork == null) {
            throw new IllegalArgumentException("Konstverk med ID " + id + " hittades inte");
        }

        existingArtwork.setPriceCategory(updatedArtwork.getPriceCategory());
        existingArtwork.setTitle(updatedArtwork.getTitle());
        existingArtwork.setArtist(updatedArtwork.getArtist());
        existingArtwork.setTechnique(updatedArtwork.getTechnique());
        existingArtwork.setSize(updatedArtwork.getSize());
        existingArtwork.setPurchaseYear(updatedArtwork.getPurchaseYear());
        existingArtwork.setLotteryYear(updatedArtwork.getLotteryYear());

        return existingArtwork;
    }

    @Transactional
    public void deleteArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id);
        if (artwork == null) {
            throw new IllegalArgumentException("Konstverk med ID " + id + " hittades inte");
        }
        artworkRepository.delete(artwork);
    }

    @Transactional
    public void markAsWon(Long id) {
        Artwork artwork = artworkRepository.findById(id);
        if (artwork == null) {
            throw new IllegalArgumentException("Konstverk med ID " + id + " hittades inte");
        }
        artwork.setIsWon(true);
    }

    public List<Artwork> getWonArtworks() {
        return artworkRepository.findWonArtworks();
    }
}
