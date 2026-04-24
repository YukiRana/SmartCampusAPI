package com.mycompany.smartcampusapi.service;

import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store. Uses ConcurrentHashMap to safely
 * handle concurrent requests under JAX-RS per-request lifecycle.
 * @author Yuki Ranathilaka
 */
public final class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {}
    public static DataStore getInstance() { return INSTANCE; }

    public Map<String, Room> getRooms() { return rooms; }
    public Room getRoom(String id) { return rooms.get(id); }
    public void putRoom(Room room) { rooms.put(room.getId(), room); }
    public boolean removeRoom(String id) { return rooms.remove(id) != null; }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Sensor getSensor(String id) { return sensors.get(id); }
    public void putSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}