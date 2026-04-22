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

    // GET /api/v1/sensors?type=CO2 
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isEmpty()) {
            return DataStore.sensors;
        }
        // Filter by type 
        return DataStore.sensors.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors – create new sensor (validates roomId exists)
    @POST
    public Response createSensor(Sensor sensor) {
        // Validate room exists (roomId is String)
        Room room = DataStore.rooms.stream()
                .filter(r -> r.getId().equals(sensor.getRoomId()))
                .findFirst()
                .orElse(null);
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " does not exist.");
        }

        // Assign new ID
        sensor.setId(DataStore.nextSensorId());
        DataStore.sensors.add(sensor);

        // Add sensor ID to the room's sensorIds list 
        room.getSensorIds().add(sensor.getId());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
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

    // Sub-resource locator for /sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}