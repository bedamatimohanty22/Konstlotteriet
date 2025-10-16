package se.konstlotteriet.rest.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.dto.MemberDTO;
import se.konstlotteriet.service.MemberService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class MemberResource {

    @Inject
    MemberService memberService;

    @GET
    public List<MemberDTO> getAllMembers() {
        return memberService.getAllMembers().stream()
                .map(MemberDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/active")
    public List<MemberDTO> getActiveMembers() {
        return memberService.getActiveMembers().stream()
                .map(MemberDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getMemberById(@PathParam("id") Long id) {
        return memberService.getMemberById(id)
                .map(MemberDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response createMember(@Valid MemberDTO memberDTO) {
        try {
            Member member = memberDTO.toEntity();
            Member created = memberService.createMember(member);
            return Response.status(Response.Status.CREATED)
                    .entity(MemberDTO.fromEntity(created))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateMember(@PathParam("id") Long id, @Valid MemberDTO memberDTO) {
        try {
            Member member = memberDTO.toEntity();
            Member updated = memberService.updateMember(id, member);
            return Response.ok(MemberDTO.fromEntity(updated)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMember(@PathParam("id") Long id) {
        try {
            memberService.deleteMember(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{id}/reactivate")
    public Response reactivateMember(@PathParam("id") Long id) {
        try {
            memberService.reactivateMember(id);
            return Response.ok().build();
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
