package se.konstlotteriet.rest.member;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import se.konstlotteriet.dto.MemberDTO;
import se.konstlotteriet.service.MemberService;

@Path("/api/member/profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "MEMBER"})
public class MemberProfileResource {

    @Inject
    MemberService memberService;

    @Inject
    JsonWebToken jwt;

    @GET
    public Response getProfile() {
        // Get memberId from JWT token
        Object memberIdClaim = jwt.getClaim("memberId");

        if (memberIdClaim == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Användaren är inte kopplad till en medlem"))
                    .build();
        }

        Long memberId = Long.parseLong(memberIdClaim.toString());

        return memberService.getMemberById(memberId)
                .map(MemberDTO::fromEntity)
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
