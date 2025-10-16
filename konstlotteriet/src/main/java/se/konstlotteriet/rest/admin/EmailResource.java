package se.konstlotteriet.rest.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.domain.LotteryParticipant;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.domain.RankingList;
import se.konstlotteriet.repository.LotteryParticipantRepository;
import se.konstlotteriet.repository.LotteryRepository;
import se.konstlotteriet.repository.MemberRepository;
import se.konstlotteriet.repository.RankingListRepository;
import se.konstlotteriet.service.EmailService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/email")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class EmailResource {

    @Inject
    EmailService emailService;

    @Inject
    LotteryRepository lotteryRepository;

    @Inject
    LotteryParticipantRepository participantRepository;

    @Inject
    MemberRepository memberRepository;

    @Inject
    RankingListRepository rankingListRepository;

    @POST
    @Path("/test")
    public Response sendTestEmail(@QueryParam("to") String to) {
        if (to == null || to.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Email address is required"))
                    .build();
        }

        try {
            emailService.sendTestEmail(to);
            return Response.ok(new SuccessResponse("Test email sent to " + to)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to send email: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/lottery/{lotteryId}/announce")
    public Response sendLotteryAnnouncement(@PathParam("lotteryId") Long lotteryId) {
        try {
            Lottery lottery = lotteryRepository.findById(lotteryId);
            if (lottery == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Lottery not found"))
                        .build();
            }

            List<LotteryParticipant> participants = participantRepository.findByLottery(lottery);
            List<Member> members = participants.stream()
                    .map(LotteryParticipant::getMember)
                    .collect(Collectors.toList());

            if (members.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("No participants found for this lottery"))
                        .build();
            }

            emailService.sendLotteryAnnouncement(lottery, members);

            return Response.ok(new SuccessResponse(
                    "Lottery announcement sent to " + members.size() + " participants"
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to send announcements: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/lottery/{lotteryId}/remind-rankings")
    public Response sendRankingReminders(@PathParam("lotteryId") Long lotteryId) {
        try {
            Lottery lottery = lotteryRepository.findById(lotteryId);
            if (lottery == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Lottery not found"))
                        .build();
            }

            // Get all participants
            List<LotteryParticipant> participants = participantRepository.findByLottery(lottery);

            // Filter those who haven't submitted ranking lists
            List<Member> membersWithoutRanking = participants.stream()
                    .map(LotteryParticipant::getMember)
                    .filter(member -> {
                        return rankingListRepository.findByLotteryAndMember(lottery, member).isEmpty();
                    })
                    .collect(Collectors.toList());

            if (membersWithoutRanking.isEmpty()) {
                return Response.ok(new SuccessResponse(
                        "All participants have submitted their ranking lists"
                )).build();
            }

            emailService.sendRankingListReminder(lottery, membersWithoutRanking);

            return Response.ok(new SuccessResponse(
                    "Ranking reminders sent to " + membersWithoutRanking.size() + " members"
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to send reminders: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/lottery/{lotteryId}/notify-winners")
    public Response sendWinnerNotifications(@PathParam("lotteryId") Long lotteryId) {
        try {
            Lottery lottery = lotteryRepository.findById(lotteryId);
            if (lottery == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Lottery not found"))
                        .build();
            }

            List<LotteryParticipant> winners = participantRepository.findByLottery(lottery).stream()
                    .filter(p -> Boolean.TRUE.equals(p.getHasWon()))
                    .collect(Collectors.toList());

            if (winners.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("No winners found for this lottery"))
                        .build();
            }

            emailService.sendWinnerNotifications(lottery, winners);

            return Response.ok(new SuccessResponse(
                    "Winner notifications sent to " + winners.size() + " members"
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to send notifications: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/member/{memberId}/welcome")
    public Response sendWelcomeEmail(@PathParam("memberId") Long memberId) {
        try {
            Member member = memberRepository.findById(memberId);
            if (member == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Member not found"))
                        .build();
            }

            emailService.sendWelcomeEmail(member);

            return Response.ok(new SuccessResponse(
                    "Welcome email sent to " + member.getEmail()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to send welcome email: " + e.getMessage()))
                    .build();
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}
