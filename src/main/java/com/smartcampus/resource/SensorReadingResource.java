package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

public class SensorReadingResource {

    private final String sensorId;

    // Constructor receives the sensorId from the parent resource (String)
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getReadings() {
        return DataStore.getReadingsForSensor(sensorId);
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Find the parent sensor
        Sensor sensor = DataStore.sensors.stream()
                .filter(s -> s.getId().equals(sensorId))
                .findFirst()
                .orElse(null);
    if (sensor == null) {
        ErrorMessage error = new ErrorMessage(
            404,
            "SENSOR_NOT_FOUND",
            "Sensor with ID " + sensorId + " does not exist."
        );
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

        // Ensure the sensor is not in MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is in maintenance mode and cannot accept readings.");
        }

        // Set ID and timestamp
        reading.setId(DataStore.nextReadingId());
        reading.setTimestamp(System.currentTimeMillis()); // epoch milliseconds

        // Store reading in DataStore 
        DataStore.addReading(sensorId, reading);

        // Update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        // Build response
        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }
}