package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Retrieve sensor list with optional query param filtering
    @GET
    public List<Sensor> fetchCampusSensors(@QueryParam("type") String type) {
        if (type == null || type.isEmpty()) {
            return DataStore.sensors;
        }
        // Apply requested filter logic
        return DataStore.sensors.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Register new hardware (Enforces validation on the parent room ID)
    @POST
    public Response installNewSensor(Sensor sensor) {
        // Confirm linked room matches an active location
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId().equals(sensor.getRoomId()))
                .findFirst()
                .orElse(null);
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " does not exist.");
        }

        // Set system identifier
        sensor.setId(DataStore.nextSensorId());
        DataStore.sensors.add(sensor);

        // Bind sensor ID to the parent room array
        room.getSensorIds().add(sensor.getId());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // Fetch detailed metadata for one sensor
    @GET
    @Path("/{sensorId}")
    public Response fetchSensorDetails(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.stream()
                .filter(s -> s.getId().equals(sensorId))
                .findFirst()
                .orElse(null);
        if (sensor == null) {
            ErrorMessage error = new ErrorMessage(404, "SENSOR_NOT_FOUND", "Sensor with ID " + sensorId + " does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
    }

    // Delegate reading endpoints to Sub-Resource Locator
    @Path("/{sensorId}/readings")
    public SensorReadingResource delegateToReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}