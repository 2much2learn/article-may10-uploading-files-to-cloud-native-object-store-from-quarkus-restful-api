package com.toomuch2learn.crud.catalogue.controller;

import com.toomuch2learn.crud.catalogue.error.ErrorCodes;
import com.toomuch2learn.crud.catalogue.exception.ImageUploadException;
import com.toomuch2learn.crud.catalogue.exception.ResourceNotFoundException;
import com.toomuch2learn.crud.catalogue.service.CatalogueCrudService;
import com.toomuch2learn.crud.catalogue.service.storage.IStorageService;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Path(CatalogueControllerAPIPaths.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class CatalogueItemImageUploadController {

    private Logger log = LoggerFactory.getLogger(CatalogueController.class);

    @Inject
    CatalogueCrudService catalogueCrudService;

    @Inject
    IStorageService storageService;

    private MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();

    @POST
    @Path(CatalogueControllerAPIPaths.UPLOAD_IMAGE)
    public Response uploadImage(@PathParam(value = "sku") String skuNumber, InputStream inputStream)  throws ResourceNotFoundException, Exception {
        // Validate skuNumber by Getting catalogue item by sku. If not available, resource not found will be thrown.
        catalogueCrudService.getCatalogueItem(skuNumber);

        // Create temp file from the uploaded image input stream
        File tempFile = createTempFile(skuNumber, inputStream);

        // Validate if the uploaded file is of type image. Else throw error
        Tika tika = new Tika();
        String mimeType = tika.detect(tempFile);
        if (!mimeType.contains("image")) {
            throw new ImageUploadException(ErrorCodes.ERR_IMAGE_UPLOAD_INVALID_FORMAT, String.format("File uploaded for SKU:%s is not valid image format", skuNumber));
        }

        // Upload the file to storage service
        storageService.uploadCatalogueImage(skuNumber, mimeType, tempFile);

        return Response.status(Response.Status.CREATED).build();
    }

    private File createTempFile(String skuNumber, InputStream inputStream) throws ImageUploadException {
        try {
            File tempFile = File.createTempFile(skuNumber, ".tmp");
            tempFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(inputStream, out);

            return tempFile;
        }
        catch(Exception e) {
            throw new ImageUploadException(String.format("Error occurred while creating temp file for uploaded image : %s", skuNumber), e);
        }
    }
}
