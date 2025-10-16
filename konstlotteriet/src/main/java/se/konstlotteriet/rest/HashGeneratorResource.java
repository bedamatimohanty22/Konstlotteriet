package se.konstlotteriet.rest;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/api/dev/hash")
@PermitAll
public class HashGeneratorResource {

    @GET
    public Response generateHash(@QueryParam("password") String password) {
        if (password == null || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Password parameter is required\"}")
                    .build();
        }

        String hash = BcryptUtil.bcryptHash(password);
        boolean matches = BcryptUtil.matches(password, hash);

        String json = String.format(
            "{\"password\":\"%s\",\"hash\":\"%s\",\"verification\":%s}",
            password, hash, matches
        );

        return Response.ok(json).build();
    }
}