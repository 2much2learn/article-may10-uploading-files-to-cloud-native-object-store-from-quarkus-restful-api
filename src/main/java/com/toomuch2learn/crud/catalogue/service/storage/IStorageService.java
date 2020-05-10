package com.toomuch2learn.crud.catalogue.service.storage;

import com.toomuch2learn.crud.catalogue.exception.ImageUploadException;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for handling storage requirements
 */
public interface IStorageService {

    /**
     * Upload catalogue image to storage service
     * @param skuNumber
     * @param contentType
     * @param image
     * @throws ImageUploadException
     */
    public void uploadCatalogueImage(String skuNumber, String contentType, File image) throws ImageUploadException;
}
