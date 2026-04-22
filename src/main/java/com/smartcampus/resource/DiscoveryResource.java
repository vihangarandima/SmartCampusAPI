package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        
        // API Version Info
        metadata.put("apiName", "Smart Campus API");
        metadata.put("version", "1.0.0");
        
        // Administrative Contact 
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Smart Campus Admin");
        contact.put("email", "admin@university.edu");
        metadata.put("contact", contact);
        
        // Resource Collections Map
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        metadata.put("resources", resources);
        
        return Response.ok(metadata).build();
    }
}