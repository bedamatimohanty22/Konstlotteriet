package se.konstlotteriet.rest.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import se.konstlotteriet.domain.Artwork;
import se.konstlotteriet.dto.ArtworkDTO;
import se.konstlotteriet.service.ArtworkService;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/artworks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class ArtworkResource {

    @Inject
    ArtworkService artworkService;

    @GET
    public List<ArtworkDTO> getAllArtworks() {
        return artworkService.getAllArtworks().stream()
                .map(ArtworkDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public Response getArtworkById(@PathParam("id") Long id) {
        return artworkService.getArtworkById(id)
                .map(ArtworkDTO::fromEntity)
                .map(dto -> Response.ok(dto).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/year/{year}")
    public List<ArtworkDTO> getArtworksByYear(@PathParam("year") Integer year) {
        return artworkService.getArtworksByYear(year).stream()
                .map(ArtworkDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/year/{year}/available")
    public List<ArtworkDTO> getAvailableArtworks(@PathParam("year") Integer year) {
        return artworkService.getAvailableArtworksForYear(year).stream()
                .map(ArtworkDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @POST
    public Response createArtwork(@Valid ArtworkDTO artworkDTO) {
        try {
            Artwork artwork = artworkDTO.toEntity();
            Artwork created = artworkService.createArtwork(artwork);
            return Response.status(Response.Status.CREATED)
                    .entity(ArtworkDTO.fromEntity(created))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateArtwork(@PathParam("id") Long id, @Valid ArtworkDTO artworkDTO) {
        try {
            Artwork artwork = artworkDTO.toEntity();
            Artwork updated = artworkService.updateArtwork(id, artwork);
            return Response.ok(ArtworkDTO.fromEntity(updated)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteArtwork(@PathParam("id") Long id) {
        try {
            artworkService.deleteArtwork(id);
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
