package com.mycompany.smartcampusapi.resources;

import java.util.List;
import java.net.URI;

import com.mycompany.smartcampusapi.dto.RoomRequest;
import com.mycompany.smartcampusapi.dto.RoomResponse;
import com.mycompany.smartcampusapi.service.RoomService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomService roomService = new RoomService();
    
    @GET
    public List<RoomResponse> getRooms() {
        return roomService.listRooms();
    }
    
    @GET
    @Path("/{id}")
    public RoomResponse getRoom(@PathParam("id") long id) {
        return roomService.getRoom(id);
    }
    
    @POST
    public Response createRoom(RoomRequest request, @Context UriInfo uriInfo) {
        RoomResponse created = roomService.createRoom(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.id())).build();
        return Response.created(location).entity(created).build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") long id) {
        roomService.deleteRoom(id);
        return Response.noContent().build();
    }
}
