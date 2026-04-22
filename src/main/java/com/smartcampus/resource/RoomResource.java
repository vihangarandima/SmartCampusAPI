package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /api/v1/rooms - List all rooms
    @GET
    public List<Room> getAllRooms() {
        return DataStore.rooms;
    }

    // GET /api/v1/rooms/{roomId} - Get a specific room
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
        if (room == null) {
            ErrorMessage error = new ErrorMessage(404, "ROOM_NOT_FOUND", "Room with ID " + roomId + " does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    // POST /api/v1/rooms - Create a new room
    @POST
    public Response createRoom(Room room) {
        // Assign a new ID (String)
        room.setId(DataStore.nextRoomId());
        DataStore.rooms.add(room);
        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} - Delete a room only if no sensors are assigned
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);
        if (room == null) {
            ErrorMessage error = new ErrorMessage(
                404,
                "ROOM_NOT_FOUND",
                "Room with ID " + roomId + " does not exist."
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        boolean hasSensors = DataStore.sensors.stream()
                .anyMatch(s -> s.getRoomId().equals(roomId));
        if (hasSensors) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted because it has active sensors.");
        }

        DataStore.rooms.remove(room);
        return Response.noContent().build();
    }
}