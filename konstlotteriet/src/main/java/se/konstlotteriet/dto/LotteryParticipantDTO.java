package se.konstlotteriet.dto;

import se.konstlotteriet.domain.LotteryParticipant;

public class LotteryParticipantDTO {

    public Long id;
    public Long lotteryId;
    public MemberDTO member;
    public Integer numberOfTickets;
    public Boolean hasWon;
    public ArtworkDTO wonArtwork;

    public LotteryParticipantDTO() {
    }

    public static LotteryParticipantDTO fromEntity(LotteryParticipant participant) {
        LotteryParticipantDTO dto = new LotteryParticipantDTO();
        dto.id = participant.getId();
        dto.lotteryId = participant.getLottery().getId();
        dto.member = MemberDTO.fromEntity(participant.getMember());
        dto.numberOfTickets = participant.getNumberOfTickets();
        dto.hasWon = participant.getHasWon();
        if (participant.getWonArtwork() != null) {
            dto.wonArtwork = ArtworkDTO.fromEntity(participant.getWonArtwork());
        }
        return dto;
    }
}
