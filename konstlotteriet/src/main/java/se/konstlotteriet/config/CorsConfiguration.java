package se.konstlotteriet.config;

import io.quarkus.arc.Unremovable;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@ApplicationScoped
@Unremovable
public class CorsConfiguration implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Only add headers if they don't already exist
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Origin")) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Credentials")) {
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Headers")) {
            responseContext.getHeaders().add("Access-Control-Allow-Headers",
                    "origin, content-type, accept, authorization");
        }
        if (!responseContext.getHeaders().containsKey("Access-Control-Allow-Methods")) {
            responseContext.getHeaders().add("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        }
    }
}