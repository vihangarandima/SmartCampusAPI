package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorMessage error = new ErrorMessage(
            403,
            "SENSOR_UNAVAILABLE",
            exception.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}