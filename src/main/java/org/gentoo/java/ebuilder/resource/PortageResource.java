package org.gentoo.java.ebuilder.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collections;
import java.util.List;

@Path("/portage")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class PortageResource {

    @GET
    public List get() {
        return Collections.emptyList();
    }

    @POST
    @Path("/update_cache")
    public void updateCache() {
        //portageModel.updateCache();
    }
}
