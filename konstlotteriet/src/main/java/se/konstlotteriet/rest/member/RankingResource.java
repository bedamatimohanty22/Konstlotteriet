package se.konstlotteriet.rest.member;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import se.konstlotteriet.domain.RankingList;
import se.konstlotteriet.dto.RankingListDTO;
import se.konstlotteriet.service.RankingService;

import java.util.List;

@Path("/api/member/rankings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MEMBER"})
public class RankingResource {

    @Inject
    RankingService rankingService;

    @Inject
    JsonWebToken jwt;

    private Long getMemberIdFromToken() {
        Object memberIdClaim = jwt.getClaim("memberId");
        if (memberIdClaim == null) {
            throw new WebApplicationException("Användaren är inte kopplad till en medlem", Response.Status.BAD_REQUEST);
        }
        return Long.parseLong(memberIdClaim.toString());
    }

    @POST
    @Path("/lottery/{lotteryId}")
    public Response createRankingList(@PathParam("lotteryId") Long lotteryId) {
        Long memberId = getMemberIdFromToken();
        try {
            RankingList created = rankingService.createRankingList(lotteryId, memberId);
            return Response.status(Response.Status.CREATED)
                    .entity(RankingListDTO.fromEntity(created))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{rankingListId}")
    public Response updateRankingList(@PathParam("rankingListId") Long rankingListId,
                                      List<RankingService.RankingItemData> rankings) {
        try {
            RankingList updated = rankingService.updateRankingList(rankingListId, rankings);
            return Response.ok(RankingListDTO.fromEntity(updated)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{rankingListId}/submit")
    public Response submitRankingList(@PathParam("rankingListId") Long rankingListId) {
        try {
            rankingService.submitRankingList(rankingListId);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/lottery/{lotteryId}")
    public Response getRankingList(@PathParam("lotteryId") Long lotteryId) {
        Long memberId = getMemberIdFromToken();
        return rankingService.getRankingListByLotteryAndMember(lotteryId, memberId)
                .map(RankingListDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{rankingListId}")
    public Response deleteRankingList(@PathParam("rankingListId") Long rankingListId) {
        try {
            rankingService.deleteRankingList(rankingListId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
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
}
