package com.smartcampus.dao;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStore {
    // Concurrency-safe generators for building unique textual IDs
    private static final AtomicInteger roomCounter = new AtomicInteger(1);
    private static final AtomicInteger sensorCounter = new AtomicInteger(1);
    private static final AtomicInteger readingCounter = new AtomicInteger(1);

    public static final List<Room> rooms = new ArrayList<>();
    public static final List<Sensor> sensors = new ArrayList<>();
    public static final Map<String, List<SensorReading>> readingsBySensor = new HashMap<>();

    // Bootstrapping the data context
    static {
        // Generate initial campus locations
        Room room1 = new Room("R" + roomCounter.getAndIncrement(), "Library", 140);
        Room room2 = new Room("R" + roomCounter.getAndIncrement(), "Lab 6", 35);
        rooms.add(room1);
        rooms.add(room2);

        // Setup base campus hardware
        Sensor sensor1 = new Sensor("S" + sensorCounter.getAndIncrement(), "CO2", "ACTIVE", 450.0, room1.getId());
        Sensor sensor2 = new Sensor("S" + sensorCounter.getAndIncrement(), "OCCUPANCY", "ACTIVE", 85.0, room1.getId());
        Sensor sensor3 = new Sensor("S" + sensorCounter.getAndIncrement(), "LIGHTING", "MAINTENANCE", 0.0,
                room2.getId());
        sensors.add(sensor1);
        sensors.add(sensor2);
        sensors.add(sensor3);

        // Link the hardware to their respective locations
        room1.getSensorIds().add(sensor1.getId());
        room1.getSensorIds().add(sensor2.getId());
        room2.getSensorIds().add(sensor3.getId());

        // Set up blank history arrays for each monitor
        readingsBySensor.put(sensor1.getId(), new ArrayList<>());
        readingsBySensor.put(sensor2.getId(), new ArrayList<>());
        readingsBySensor.put(sensor3.getId(), new ArrayList<>());

        // Seed some historical metrics
        long currentTimeMs = System.currentTimeMillis();
        addReading(sensor1.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), currentTimeMs - 3600000, 450.0));
        addReading(sensor1.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), currentTimeMs, 455.0));
        addReading(sensor2.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), currentTimeMs - 1800000, 85.0));
    }

    public static String nextRoomId() {
        return "R" + roomCounter.getAndIncrement();
    }

    public static String nextSensorId() {
        return "S" + sensorCounter.getAndIncrement();
    }

    public static String nextReadingId() {
        return "RD" + readingCounter.getAndIncrement();
    }

    public static List<SensorReading> getReadingsForSensor(String sensorId) {
        return readingsBySensor.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        readingsBySensor.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}