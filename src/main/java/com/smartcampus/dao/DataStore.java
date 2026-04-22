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
    // Thread-safe numeric counters for generating unique String IDs
    private static final AtomicInteger roomCounter = new AtomicInteger(1);
    private static final AtomicInteger sensorCounter = new AtomicInteger(1);
    private static final AtomicInteger readingCounter = new AtomicInteger(1);

    public static final List<Room> rooms = new ArrayList<>();
    public static final List<Sensor> sensors = new ArrayList<>();
    public static final Map<String, List<SensorReading>> readingsBySensor = new HashMap<>();

    // Static initializer with sample data
    static {
        // Add initial rooms with String IDs
        Room room1 = new Room("R" + roomCounter.getAndIncrement(), "Lecture Hall A", 120);
        Room room2 = new Room("R" + roomCounter.getAndIncrement(), "Lab 3", 30);
        rooms.add(room1);
        rooms.add(room2);

        // Add initial sensors
        Sensor sensor1 = new Sensor("S" + sensorCounter.getAndIncrement(), "CO2", "ACTIVE", 450.0, room1.getId());
        Sensor sensor2 = new Sensor("S" + sensorCounter.getAndIncrement(), "OCCUPANCY", "ACTIVE", 85.0, room1.getId());
        Sensor sensor3 = new Sensor("S" + sensorCounter.getAndIncrement(), "LIGHTING", "MAINTENANCE", 0.0, room2.getId());
        sensors.add(sensor1);
        sensors.add(sensor2);
        sensors.add(sensor3);

        // Add sensor IDs to corresponding rooms
        room1.getSensorIds().add(sensor1.getId());
        room1.getSensorIds().add(sensor2.getId());
        room2.getSensorIds().add(sensor3.getId());

        // Initialize empty reading lists for each sensor
        readingsBySensor.put(sensor1.getId(), new ArrayList<>());
        readingsBySensor.put(sensor2.getId(), new ArrayList<>());
        readingsBySensor.put(sensor3.getId(), new ArrayList<>());

        // Sample readings
        long now = System.currentTimeMillis();
        addReading(sensor1.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), now - 3600000, 450.0));
        addReading(sensor1.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), now, 455.0));
        addReading(sensor2.getId(), new SensorReading("RD" + readingCounter.getAndIncrement(), now - 1800000, 85.0));
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