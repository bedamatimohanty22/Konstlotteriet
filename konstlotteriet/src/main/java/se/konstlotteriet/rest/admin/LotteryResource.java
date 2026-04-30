package se.konstlotteriet.rest.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.konstlotteriet.domain.Lottery;
import se.konstlotteriet.dto.LotteryDTO;
import se.konstlotteriet.dto.LotteryParticipantDTO;
import se.konstlotteriet.service.EmailService;
import se.konstlotteriet.service.LotteryService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/lotteries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class LotteryResource {

    @Inject
    LotteryService lotteryService;

    @Inject
    EmailService emailService;

    @GET
    public List<LotteryDTO> getAllLotteries() {
        return lotteryService.getAllLotteries().stream()
                .map(LotteryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getLotteryById(@PathParam("id") Long id) {
        return lotteryService.getLotteryById(id)
                .map(LotteryDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/year/{year}")
    public Response getLotteryByYear(@PathParam("year") Integer year) {
        return lotteryService.getLotteryByYear(year)
                .map(LotteryDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/active")
    public Response getActiveLottery() {
        return lotteryService.getActiveLottery()
                .map(LotteryDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response createLottery(@Valid LotteryDTO lotteryDTO) {
        try {
            Lottery lottery = lotteryDTO.toEntity();
            Lottery created = lotteryService.createLottery(lottery);
            return Response.status(Response.Status.CREATED)
                    .entity(LotteryDTO.fromEntity(created))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateLottery(@PathParam("id") Long id, @Valid LotteryDTO lotteryDTO) {
        try {
            Lottery lottery = lotteryDTO.toEntity();
            Lottery updated = lotteryService.updateLottery(id, lottery);
            return Response.ok(LotteryDTO.fromEntity(updated)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteLottery(@PathParam("id") Long id) {
        try {
            lotteryService.deleteLottery(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/participants/add-all")
    public Response addAllMembersToLottery(@PathParam("id") Long id) {
        try {
            lotteryService.addMembersToLottery(id);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{lotteryId}/participants/{memberId}")
    public Response addMemberToLottery(@PathParam("lotteryId") Long lotteryId,
                                      @PathParam("memberId") Long memberId) {
        try {
            lotteryService.addMemberToLottery(lotteryId, memberId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}/participants")
    public List<LotteryParticipantDTO> getParticipants(@PathParam("id") Long id) {
        return lotteryService.getLotteryParticipants(id).stream()
                .map(LotteryParticipantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}/winners")
    public List<LotteryParticipantDTO> getWinners(@PathParam("id") Long id) {
        return lotteryService.getWinners(id).stream()
                .map(LotteryParticipantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @POST
    @Path("/{id}/activate")
    public Response activateLottery(@PathParam("id") Long id) {
        try {
            lotteryService.activateLottery(id);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/complete")
    public Response completeLottery(@PathParam("id") Long id) {
        try {
            lotteryService.completeLottery(id);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Send ranking list email to all active members
     */
    @POST
    @Path("/{id}/send-ranking-lists")
    public Response sendRankingLists(@PathParam("id") Long id,
                                     @QueryParam("loginUrl") String loginUrl) {
        try {
            emailService.sendRankingListToAllMembers(id, loginUrl);
            return Response.ok(new SuccessResponse("Rankinglistor skickade till alla medlemmar")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Send ranking list email to a specific member
     */
    @POST
    @Path("/{lotteryId}/send-ranking-list/{memberId}")
    public Response sendRankingListToMember(@PathParam("lotteryId") Long lotteryId,
                                           @PathParam("memberId") Long memberId,
                                           @QueryParam("loginUrl") String loginUrl) {
        try {
            emailService.sendRankingListToMember(lotteryId, memberId, loginUrl);
            return Response.ok(new SuccessResponse("Rankingslista skickad till medlem")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Send lottery results email to all participants
     */
    @POST
    @Path("/{id}/send-results")
    public Response sendLotteryResults(@PathParam("id") Long id) {
        try {
            emailService.sendLotteryResultsToAllParticipants(id);
            return Response.ok(new SuccessResponse("Lotteriresultat skickade till alla deltagare")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Send lottery results email to a specific member
     */
    @POST
    @Path("/{lotteryId}/send-result/{memberId}")
    public Response sendLotteryResultToMember(@PathParam("lotteryId") Long lotteryId,
                                             @PathParam("memberId") Long memberId) {
        try {
            emailService.sendLotteryResultsToMember(lotteryId, memberId);
            return Response.ok(new SuccessResponse("Lotteriresultat skickat till medlem")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage()))
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
