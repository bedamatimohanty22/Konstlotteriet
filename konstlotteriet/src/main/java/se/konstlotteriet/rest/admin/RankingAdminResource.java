package se.konstlotteriet.rest.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.konstlotteriet.dto.RankingListDTO;
import se.konstlotteriet.service.RankingService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/rankings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class RankingAdminResource {

    @Inject
    RankingService rankingService;

    @GET
    @Path("/lottery/{lotteryId}")
    public List<RankingListDTO> getRankingsByLottery(@PathParam("lotteryId") Long lotteryId) {
        return rankingService.getRankingListsByLottery(lotteryId).stream()
                .map(RankingListDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/lottery/{lotteryId}/member/{memberId}")
    public Response getRankingByLotteryAndMember(@PathParam("lotteryId") Long lotteryId,
                                                  @PathParam("memberId") Long memberId) {
        return rankingService.getRankingListByLotteryAndMember(lotteryId, memberId)
                .map(RankingListDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
