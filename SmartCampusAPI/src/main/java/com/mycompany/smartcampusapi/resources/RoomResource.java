package com.mycompany.smartcampusapi.resources;

import com.mycompany.smartcampusapi.exception.ResourceNotFoundException;
import com.mycompany.smartcampusapi.exception.RoomNotEmptyException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.service.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;

/** @author Yuki Ranathilaka */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        rooms.sort(Comparator.comparing(Room::getId));
        return Response.ok(rooms).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) throw new ResourceNotFoundException("Room '" + roomId + "' was not found.");
        return Response.ok(room).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getName() == null || room.getName().isBlank()) {
            throw new WebApplicationException(Response.status(400)
                .entity(err(400, "Bad Request", "Room 'name' is required.")).build());
        }
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId("ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (store.getRoom(room.getId()) != null) {
            throw new RoomNotEmptyException("Room ID '" + room.getId() + "' already exists.");
        }
        if (room.getSensorIds() == null) room.setSensorIds(new ArrayList<>());
        store.putRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) throw new ResourceNotFoundException("Room '" + roomId + "' was not found.");
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room '" + roomId + "' cannot be deleted because it still has "
                + room.getSensorIds().size() + " sensor(s) assigned to it. "
                + "Remove all sensors before attempting deletion.");
        }
        store.removeRoom(roomId);
        return Response.noContent().build();
    }

    private Map<String, Object> err(int s, String e, String m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", s); map.put("error", e);
        map.put("message", m); map.put("timestamp", Instant.now().toString());
        return map;
    }
}
