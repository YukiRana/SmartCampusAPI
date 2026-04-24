package com.mycompany.smartcampusapi.service;

import java.util.List;

import com.mycompany.smartcampusapi.dto.RoomRequest;
import com.mycompany.smartcampusapi.dto.RoomResponse;
import com.mycompany.smartcampusapi.exception.ConflictException;
import com.mycompany.smartcampusapi.exception.ResourceNotFoundException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.util.InputValidator;

import jakarta.persistence.EntityManager;

public class RoomService extends JpaSupport {

    public List<RoomResponse> listRooms() {
        return execute(entityManager -> entityManager.createQuery(
                        "select r from Room r order by lower(r.name), r.id",
                        Room.class)
                .getResultList()
                .stream()
                .map(this::toResponse)
                .toList());
    }

    public RoomResponse getRoom(long roomId) {
        InputValidator.requirePositiveId(roomId, "roomId");
        return execute(entityManager -> toResponse(findRoomEntity(entityManager, roomId)));
    }

    public RoomResponse createRoom(RoomRequest request) {
        InputValidator.requireNonNull(request, "request");
        String name = InputValidator.requireNonBlank(request.name(), "name", 120);
        return execute(entityManager -> {
            Room room = new Room();
            room.setName(name);
            entityManager.persist(room);
            entityManager.flush();
            return toResponse(room);
        });
    }

    public void deleteRoom(long roomId) {
        InputValidator.requirePositiveId(roomId, "roomId");
        execute(entityManager -> {
            Room room = findRoomEntity(entityManager, roomId);
            if (!room.getSensors().isEmpty()) {
                throw new ConflictException("Room has sensors and cannot be deleted.");
            }
            entityManager.remove(room);
            return null;
        });
    }

    Room findRoomEntity(EntityManager entityManager, long roomId) {
        Room room = entityManager.find(Room.class, roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room " + roomId + " was not found.");
        }
        return room;
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getSensors().size());
    }
}