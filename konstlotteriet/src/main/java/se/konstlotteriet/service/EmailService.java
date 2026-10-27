package se.konstlotteriet.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.konstlotteriet.domain.Artwork;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.LotteryParticipant;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.repository.LotteryParticipantRepository;
import se.konstlotteriet.repository.LotteryRepository;
import se.konstlotteriet.repository.MemberRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    LotteryParticipantRepository lotteryParticipantRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Send lottery announcement email to all participants
     */
    public void sendLotteryAnnouncement(Lottery lottery, List<Member> participants) {
        String subject = "Ny utlottning " + lottery.getYear() + " - Konstlotteriet";

        for (Member member : participants) {
            String body = buildLotteryAnnouncementEmail(member, lottery);
            sendEmail(member.getEmail(), subject, body);
        }
    }

    /**
     * Send ranking list reminder to members who haven't submitted
     */
    public void sendRankingListReminder(Lottery lottery, List<Member> membersWithoutRanking) {
        String subject = "Påminnelse: Skicka in din rankinglista - Konstlotteriet";

        for (Member member : membersWithoutRanking) {
            String body = buildRankingReminderEmail(member, lottery);
            sendEmail(member.getEmail(), subject, body);
        }
    }

    /**
     * Send winner notification emails after draw
     */
    public void sendWinnerNotifications(Lottery lottery, List<LotteryParticipant> winners) {
        for (LotteryParticipant winner : winners) {
            String subject = "Grattis! Du har vunnit i Konstlotteriet " + lottery.getYear();
            String body = buildWinnerEmail(winner.getMember(), winner.getWonArtwork(), lottery);
            sendEmail(winner.getMember().getEmail(), subject, body);
        }
    }

    /**
     * Send draw completion notification to all participants
     */
    public void sendDrawCompletionNotification(Lottery lottery, List<LotteryParticipant> allParticipants) {
        String subject = "Dragning genomförd - Konstlotteriet " + lottery.getYear();

        for (LotteryParticipant participant : allParticipants) {
            String body = Boolean.TRUE.equals(participant.getHasWon())
                ? buildWinnerEmail(participant.getMember(), participant.getWonArtwork(), lottery)
                : buildNonWinnerEmail(participant.getMember(), lottery);

            sendEmail(participant.getMember().getEmail(), subject, body);
        }
    }

    /**
     * Send welcome email to new members
     */
    public void sendWelcomeEmail(Member member) {
        String subject = "Välkommen till Konstlotteriet!";
        String body = buildWelcomeEmail(member);
        sendEmail(member.getEmail(), subject, body);
    }

    public void sendRankingListToAllMembers(Long lotteryId, String loginUrl) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Lottning med ID " + lotteryId + " hittades inte");
        }

        for (Member member : memberRepository.findActiveMembers()) {
            String subject = "Din rankinglista för Konstlotteriet " + lottery.getYear();
            String body = buildRankingListReminder(member, lottery, loginUrl);
            sendEmail(member.getEmail(), subject, body);
        }
    }

    public void sendRankingListToMember(Long lotteryId, Long memberId, String loginUrl) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        Member member = memberRepository.findById(memberId);

        if (lottery == null) {
            throw new IllegalArgumentException("Lottning med ID " + lotteryId + " hittades inte");
        }
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + memberId + " hittades inte");
        }

        String subject = "Din rankinglista för Konstlotteriet " + lottery.getYear();
        String body = buildRankingListReminder(member, lottery, loginUrl);
        sendEmail(member.getEmail(), subject, body);
    }

    public void sendLotteryResultsToAllParticipants(Long lotteryId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        if (lottery == null) {
            throw new IllegalArgumentException("Lottning med ID " + lotteryId + " hittades inte");
        }

        for (LotteryParticipant participant : lotteryParticipantRepository.findByLotteryId(lotteryId)) {
            String subject = "Lotteriresultat för Konstlotteriet " + lottery.getYear();
            String body = Boolean.TRUE.equals(participant.getHasWon())
                ? buildWinnerEmail(participant.getMember(), participant.getWonArtwork(), lottery)
                : buildNonWinnerEmail(participant.getMember(), lottery);
            sendEmail(participant.getMember().getEmail(), subject, body);
        }
    }

    public void sendLotteryResultsToMember(Long lotteryId, Long memberId) {
        Lottery lottery = lotteryRepository.findById(lotteryId);
        Member member = memberRepository.findById(memberId);
        if (lottery == null) {
            throw new IllegalArgumentException("Lottning med ID " + lotteryId + " hittades inte");
        }
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + memberId + " hittades inte");
        }

        LotteryParticipant participant = lotteryParticipantRepository.findByLotteryAndMember(lottery, member)
            .orElse(null);
        if (participant == null) {
            throw new IllegalArgumentException("Medlemmen deltar inte i denna utlottning");
        }

        String subject = "Lotteriresultat för Konstlotteriet " + lottery.getYear();
        String body = Boolean.TRUE.equals(participant.getHasWon())
            ? buildWinnerEmail(participant.getMember(), participant.getWonArtwork(), lottery)
            : buildNonWinnerEmail(participant.getMember(), lottery);
        sendEmail(participant.getMember().getEmail(), subject, body);
    }

    private String buildRankingListReminder(Member member, Lottery lottery, String loginUrl) {
        return String.format("""
            Hej %s,

            Detta är en påminnelse om att du kan fylla i din rankinglista för årets utlottning %d.

            Dragningsdatum: %s

            Logga in här för att skapa eller uppdatera din rankinglista:
            %s

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName(),
            lottery.getYear(),
            lottery.getLotteryDate() != null ? lottery.getLotteryDate().format(DATE_FORMATTER) : "Ej satt",
            loginUrl != null && !loginUrl.isBlank() ? loginUrl : "[LÄNK TILL WEBBPLATS]"
        );
    }

    /**
     * Send email (actual sending)
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            mailer.send(
                Mail.withText(to, subject, body)
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // ========== Email Templates ==========

    private String buildLotteryAnnouncementEmail(Member member, Lottery lottery) {
        return String.format("""
            Hej %s,

            Vi har glädjen att meddela att årets utlottning för %d nu är öppen!

            Dragningsdatum: %s

            För att delta i utlottningen behöver du skicka in din rankinglista med
            dina önskemål om vilka konstverk du helst vill vinna.

            Logga in på vår webbplats för att se årets konstverk och skapa din rankinglista:
            [LÄNK TILL WEBBPLATS]

            Kom ihåg att skicka in din lista innan dragningsdatumet!

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName(),
            lottery.getYear(),
            lottery.getLotteryDate() != null ? lottery.getLotteryDate().format(DATE_FORMATTER) : "Ej satt"
        );
    }

    private String buildRankingReminderEmail(Member member, Lottery lottery) {
        return String.format("""
            Hej %s,

            Detta är en påminnelse om att du ännu inte har skickat in din rankinglista
            för årets utlottning %d.

            Dragningsdatum: %s

            För att delta i dragningen måste du skicka in din rankinglista innan dragningen.
            Logga in på webbplatsen för att skapa din lista:
            [LÄNK TILL WEBBPLATS]

            Om du inte skickar in en rankinglista kommer du fortfarande att delta i dragningen,
            men då får du ett slumpmässigt konstverk om du vinner.

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName(),
            lottery.getYear(),
            lottery.getLotteryDate() != null ? lottery.getLotteryDate().format(DATE_FORMATTER) : "Ej satt"
        );
    }

    private String buildWinnerEmail(Member member, Artwork artwork, Lottery lottery) {
        return String.format("""
            Grattis %s!

            Vi har glädjen att meddela att du har vunnit i Konstlotteriet %d!

            Du har vunnit följande konstverk:

            Titel: %s
            Konstnär: %s
            Teknik: %s
            Storlek: %s
            Priskategori: %s

            Information om när och var du kan hämta ditt konstverk kommer att skickas
            separat.

            Stort grattis!

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName(),
            lottery.getYear(),
            artwork.getTitle(),
            artwork.getArtist(),
            artwork.getTechnique(),
            artwork.getSize(),
            artwork.getPriceCategory()
        );
    }

    private String buildNonWinnerEmail(Member member, Lottery lottery) {
        return String.format("""
            Hej %s,

            Tack för din medverkan i Konstlotteriet %d!

            Tyvärr blev du inte utvald i årets dragning, men vi hoppas att du
            fortsätter att delta i kommande utlottningar.

            Nästa år får du automatiskt en ny lott (eller fler beroende på reglerna),
            vilket ökar dina chanser att vinna!

            Vi ser fram emot din medverkan nästa år.

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName(),
            lottery.getYear()
        );
    }

    private String buildWelcomeEmail(Member member) {
        return String.format("""
            Välkommen till Konstlotteriet, %s!

            Vi är glada att ha dig som medlem. Varje år arrangerar vi en utlottning
            där våra medlemmar har chansen att vinna konstverk.

            Hur fungerar det?

            1. Vi köper in konstverk under året
            2. Vid utlottningstillfället får du rangordna konstverken efter dina preferenser
            3. En dragning genomförs där dina preferenser och antal lotter spelar roll
            4. Om du vinner får du ditt konstverk!

            Lottregler:
            - Du får 3 lotter per år (normalt)
            - Om du vann förra året får du 1 lott innevarande år
            - Du kan endast vinna en gång per år

            Logga in på vår webbplats för att se konstverk och delta i utlottningar:
            [LÄNK TILL WEBBPLATS]

            Vi ser fram emot din medverkan!

            Med vänliga hälsningar,
            Konstlotteriet
            """,
            member.getFirstName()
        );
    }

    /**
     * Send test email (for configuration testing)
     */
    public void sendTestEmail(String to) {
        String subject = "Test - Konstlotteriet Email System";
        String body = """
            Detta är ett testmail från Konstlotteriet.

            Om du får detta mail fungerar email-systemet korrekt!

            Med vänliga hälsningar,
            Konstlotteriet
            """;

        sendEmail(to, subject, body);
    }
}
