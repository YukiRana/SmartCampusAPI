package com.mycompany.smartcampusapi.service;

import com.mycompany.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.mycompany.smartcampusapi.exception.RoomNotEmptyException;
import com.mycompany.smartcampusapi.exception.SensorUnavailableException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store. All data lives here as static final maps.
 *
 * JAX-RS creates a new resource instance per request, so resource class fields
 * cannot hold persistent state. These static ConcurrentHashMap fields survive
 * for the full lifetime of the deployed application and are safely shared across
 * all concurrent request threads.
 *
 * Pre-seeded with demo data so the API is immediately demonstrable on first boot.
 *
 * @author Yuki Ranathilaka
 */
public final class DataStore {

    private static final Map<String, Room>              ROOMS   = new ConcurrentHashMap<>();
    private static final Map<String, Sensor>            SENSORS = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> READINGS = new ConcurrentHashMap<>();

    static {
        seedDemoData();
    }

    private DataStore() {
    }

    // ------------------------------------------------------------------
    // Demo seed â€” provides ready data for Postman/video demonstration
    // ------------------------------------------------------------------
    private static void seedDemoData() {
        Room lib301 = new Room("LIB-301", "Library Quiet Study",  120);
        Room lab201 = new Room("LAB-201", "AI Laboratory",         40);
        Room hall1  = new Room("HALL-1",  "Main Lecture Hall",    300);

        ROOMS.put(lib301.getId(), lib301);
        ROOMS.put(lab201.getId(), lab201);
        ROOMS.put(hall1.getId(),  hall1);

        Sensor temp001 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      22.5,  "LIB-301");
        Sensor co2001  = new Sensor("CO2-001",  "CO2",         "MAINTENANCE", 410.0, "LAB-201");
        Sensor occ001  = new Sensor("OCC-001",  "Occupancy",   "ACTIVE",      85.0,  "HALL-1");

        SENSORS.put(temp001.getId(), temp001);
        SENSORS.put(co2001.getId(),  co2001);
        SENSORS.put(occ001.getId(),  occ001);

        lib301.getSensorIds().add("TEMP-001");
        lab201.getSensorIds().add("CO2-001");
        hall1.getSensorIds().add("OCC-001");

        List<SensorReading> tempR = new ArrayList<>();
        tempR.add(new SensorReading(UUID.randomUUID().toString(),
                System.currentTimeMillis(), 22.5));
        READINGS.put("TEMP-001", tempR);

        List<SensorReading> co2R = new ArrayList<>();
        co2R.add(new SensorReading(UUID.randomUUID().toString(),
                System.currentTimeMillis(), 410.0));
        READINGS.put("CO2-001", co2R);

        List<SensorReading> occR = new ArrayList<>();
        occR.add(new SensorReading(UUID.randomUUID().toString(),
                System.currentTimeMillis(), 85.0));
        READINGS.put("OCC-001", occR);
    }

    // ------------------------------------------------------------------
    // Room operations
    // ------------------------------------------------------------------

    public static List<Room> getAllRooms() {
        return new ArrayList<>(ROOMS.values());
    }

    public static Room getRoom(String roomId) {
        return ROOMS.get(roomId);
    }

    /**
     * @return true if created; false if ID already exists (caller returns 409)
     */
    public static synchronized boolean addRoom(Room room) {
        if (ROOMS.containsKey(room.getId())) {
            return false;
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        ROOMS.put(room.getId(), room);
        return true;
    }

    /**
     * Throws RoomNotEmptyException (-> 409) if sensors still assigned.
     * @return true if deleted; false if not found (caller returns 404)
     */
    public static synchronized boolean deleteRoom(String roomId) {
        Room room = ROOMS.get(roomId);
        if (room == null) {
            return false;
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted: it still has "
                + room.getSensorIds().size() + " sensor(s) assigned to it. "
                + "Remove all sensors from the room before attempting deletion.");
        }
        ROOMS.remove(roomId);
        return true;
    }

    // ------------------------------------------------------------------
    // Sensor operations
    // ------------------------------------------------------------------

    public static List<Sensor> getAllSensors() {
        return new ArrayList<>(SENSORS.values());
    }

    public static Sensor getSensor(String sensorId) {
        return SENSORS.get(sensorId);
    }

    /**
     * Returns all sensors, optionally filtered by type (case-insensitive, partial match).
     */
    public static List<Sensor> getSensorsByType(String type) {
        List<Sensor> result = new ArrayList<>();
        if (type == null || type.trim().isEmpty()) {
            result.addAll(SENSORS.values());
            return result;
        }
        String filter = type.trim().toLowerCase();
        for (Sensor s : SENSORS.values()) {
            if (s.getType() != null && s.getType().toLowerCase().contains(filter)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Throws LinkedResourceNotFoundException (-> 422) if roomId not found.
     * @return true if created; false if sensor ID already exists (caller returns 409)
     */
    public static synchronized boolean addSensor(Sensor sensor) {
        Room room = ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: roomId '" + sensor.getRoomId()
                + "' does not exist. Create the room first.");
        }
        if (SENSORS.containsKey(sensor.getId())) {
            return false;
        }
        SENSORS.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        READINGS.put(sensor.getId(), new ArrayList<>());
        return true;
    }

    // ------------------------------------------------------------------
    // Reading operations
    // ------------------------------------------------------------------

    public static List<SensorReading> getReadings(String sensorId) {
        List<SensorReading> list = READINGS.get(sensorId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    /**
     * Throws SensorUnavailableException (-> 403) if sensor is in MAINTENANCE.
     * Side effect: updates the parent Sensor's currentValue for data consistency.
     * @return the saved SensorReading
     */
    public static synchronized SensorReading addReading(String sensorId,
                                                         SensorReading reading) {
        Sensor sensor = SENSORS.get(sensorId);
        if (sensor == null) {
            return null;
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId
                + "' is currently in MAINTENANCE and cannot accept new readings. "
                + "Change the sensor status to ACTIVE first.");
        }
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        READINGS.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        // Side effect: keep parent sensor currentValue in sync with latest reading
        sensor.setCurrentValue(reading.getValue());
        return reading;
    }
}
