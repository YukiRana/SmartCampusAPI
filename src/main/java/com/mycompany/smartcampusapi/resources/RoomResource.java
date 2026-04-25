package com.mycompany.smartcampusapi.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.mycompany.smartcampusapi.model.ApiError;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.service.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Room resource handling CRUD at /api/v1/rooms.
 *
 * Returning full objects vs IDs only:
 * Returning full Room objects in list responses gives clients all fields
 * (id, name, capacity, sensorIds) in a single round-trip with no follow-up
 * requests needed. Returning IDs only would reduce payload size but creates the
 * N+1 problem: a client must fire one GET per room to retrieve details, which
 * multiplies latency under a large collection. For a Smart Campus system with
 * a manageable room count, full objects are the right trade-off.
 *
 * DELETE idempotency:
 * The first DELETE on an existing room returns 204 No Content.
 * A second identical DELETE returns 404 Not Found because the room is gone.
 * The server-side state after both calls is identical â€” the room does not exist.
 * The HTTP specification defines idempotency as identical effect on server state,
 * not identical response codes, so DELETE is idempotent. The 409 guard prevents
 * accidental orphaning of sensors that still reference the parent room.
 *
 * @author Yuki Ranathilaka
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms() {
        List<Room> rooms = DataStore.getAllRooms();
        rooms.sort((a, b) -> a.getId().compareTo(b.getId()));
        return Response.ok(rooms).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId,
                                @Context UriInfo uriInfo) {
        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found",
                            "Room '" + roomId + "' was not found.",
                            uriInfo.getPath()))
                    .build();
        }
        return Response.ok(room).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null
                || isBlank(room.getName())
                || room.getCapacity() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiError(400, "Bad Request",
                            "Room must include a non-blank 'name' and "
                            + "a 'capacity' greater than 0.",
                            uriInfo.getPath()))
                    .build();
        }
        if (isBlank(room.getId())) {
            room.setId("ROOM-" + UUID.randomUUID().toString()
                    .substring(0, 8).toUpperCase());
        }
        Room cleanRoom = new Room(
                room.getId().trim(),
                room.getName().trim(),
                room.getCapacity());

        boolean created = DataStore.addRoom(cleanRoom);
        if (!created) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError(409, "Conflict",
                            "A room with id '" + cleanRoom.getId() + "' already exists.",
                            uriInfo.getPath()))
                    .build();
        }
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(cleanRoom.getId()).build();
        return Response.created(location).entity(cleanRoom).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId,
                               @Context UriInfo uriInfo) {
        // DataStore.deleteRoom throws RoomNotEmptyException (-> 409)
        // if sensors are still assigned â€” caught by RoomNotEmptyExceptionMapper
        boolean deleted = DataStore.deleteRoom(roomId);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ApiError(404, "Not Found",
                            "Room '" + roomId + "' was not found.",
                            uriInfo.getPath()))
                    .build();
        }
        return Response.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
