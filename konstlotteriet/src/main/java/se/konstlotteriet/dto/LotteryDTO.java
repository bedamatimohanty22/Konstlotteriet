package se.konstlotteriet.dto;

import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.LotteryStatus;

import java.time.LocalDate;

public class LotteryDTO {

    public Long id;
    public Integer year;
    public LocalDate lotteryDate;
    public String status;

    public LotteryDTO() {
    }

    public static LotteryDTO fromEntity(Lottery lottery) {
        LotteryDTO dto = new LotteryDTO();
        dto.id = lottery.getId();
        dto.year = lottery.getYear();
        dto.lotteryDate = lottery.getLotteryDate();
        dto.status = lottery.getStatus().name();
        return dto;
    }

    public Lottery toEntity() {
        Lottery lottery = new Lottery();
        lottery.setYear(this.year);
        lottery.setLotteryDate(this.lotteryDate);
        if (this.status != null) {
            lottery.setStatus(LotteryStatus.valueOf(this.status));
        }
        return lottery;
    }
}
