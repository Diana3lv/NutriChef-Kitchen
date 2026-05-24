package org.dsoft.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import org.dsoft.client.GroqClient;

@Path("/api/health")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SystemHealthController {

    @Inject
    GroqClient groqClient;

    @GET
    public Response health() {
        Map<String, String> status = new HashMap<>();
        status.put("app", "healthy");
        return Response.ok(status).build();
    }

    @GET
    @Path("/groq")
    public Response groqHealth() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean available = groqClient.isAvailable();
            status.put("service", "groq");
            status.put("available", available);
            status.put("model", "llama3-8b-8192");
            
            if (available) {
                status.put("message", "Groq LLM service is ready");
                return Response.ok(status).build();
            } else {
                status.put("message", "Groq LLM service is not available");
                return Response.status(503)
                    .entity(status)
                    .build();
            }
        } catch (Exception e) {
            status.put("error", e.getMessage());
            status.put("available", false);
            return Response.status(503)
                .entity(status)
                .build();
        }
    }

    @GET
    @Path("/detailed")
    public Response detailedHealth() {
        Map<String, Object> status = new HashMap<>();
        Map<String, Boolean> services = new HashMap<>();
        
        try {
            services.put("app", true);
            
            boolean groqAvailable = groqClient.isAvailable();
            services.put("groq", groqAvailable);
            
            status.put("status", services.containsValue(false) ? "degraded" : "healthy");
            status.put("services", services);
            
            int httpStatus = services.containsValue(false) ? 503 : 200;
            return Response.status(httpStatus)
                .entity(status)
                .build();
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
            return Response.status(500)
                .entity(status)
                .build();
        }
    }
}