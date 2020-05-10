package com.toomuch2learn.crud.catalogue.exception.mapper;

import com.toomuch2learn.crud.catalogue.error.Error;
import com.toomuch2learn.crud.catalogue.error.ErrorCodes;
import com.toomuch2learn.crud.catalogue.error.ErrorResponse;
import com.toomuch2learn.crud.catalogue.exception.ImageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ImageUploadExceptionMapper implements ExceptionMapper<ImageUploadException> {

    private Logger log = LoggerFactory.getLogger(ImageUploadExceptionMapper.class);

    @Override
    public Response toResponse(ImageUploadException e) {
        log.error(String.format("Storage exception occurred: %s ", e.getMessage()), e);

        ErrorResponse error = new ErrorResponse();

        if(e.getCode() == ErrorCodes.ERR_IMAGE_UPLOAD_INVALID_FORMAT) {
            error.getErrors().add(
                new Error(
                    ErrorCodes.ERR_IMAGE_UPLOAD_INVALID_FORMAT,
                    "Uploaded image is not of a valid format",
                    e.getMessage()
                )
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        else {
            error.getErrors().add(
                new Error(
                    ErrorCodes.ERR_IMAGE_UPLOAD_FAILED,
                    "Error occurred while uploading image",
                    e.getMessage()
                )
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
