package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unhandled exception caught", exception);
        ErrorMessage error = new ErrorMessage(
            500,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please contact support."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}