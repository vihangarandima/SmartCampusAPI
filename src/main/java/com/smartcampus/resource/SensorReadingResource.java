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

    // State passed down from the parent path
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Fetch full history log
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> extractReadingsLog() {
        return DataStore.getReadingsForSensor(sensorId);
    }

    // Append a new data metric, altering parent state
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushNewMetric(SensorReading reading) {
        // Retrieve associated sensor
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

        // Block requests if hardware is inactive
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is in maintenance mode and cannot accept readings.");
        }

        // Auto-assign properties
        reading.setId(DataStore.nextReadingId());
        reading.setTimestamp(System.currentTimeMillis()); // Set time

        // Commit to datastore
        DataStore.addReading(sensorId, reading);

        // Critical state synchronisation (side-effect)
        sensor.setCurrentValue(reading.getValue());

        // Formulate correct URI for 201 Created Header
        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }
}