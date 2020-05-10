package com.toomuch2learn.crud.catalogue.service.storage;

import com.toomuch2learn.crud.catalogue.config.MinIOConfiguration;
import com.toomuch2learn.crud.catalogue.exception.ImageUploadException;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.MalformedInputException;

/**
 * Class to handle storage operations with MinIO Object Store application
 */
@ApplicationScoped
public class MinIOStorageService implements IStorageService {

    private Logger log = LoggerFactory.getLogger(MalformedInputException.class);

    @Inject
    Engine templateEngine;

    @Inject
    MinIOConfiguration minIOConfiguration;

    private MinioClient minioClient;
    private Template policyTemplate;

    void initializeMinIOClient() throws ImageUploadException, RuntimeException {
        try {
            // Create instance of minio client with configured service details
            minioClient =
                new MinioClient(
                    minIOConfiguration.getHost(),
                    minIOConfiguration.getPort(),
                    minIOConfiguration.getAccessKey(),
                    minIOConfiguration.getSecretKey(),
                    minIOConfiguration.isUseSsl());

            // Check and create if bucket is available to store catalogue images
            createBucketIfNotExists();
        }
        catch (InvalidEndpointException | InvalidPortException e) {
            throw new RuntimeException(String.format("MinIO Service is not initialized due to invalid Host:%s or PORT:%s", "", ""));
        }
        catch (ImageUploadException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Error occurred while initializing MinIO Service", e);
        }
    }

    private void createBucketIfNotExists() throws ImageUploadException{
        try {
            // Check if the bucket already exists.
            boolean isExist = minioClient.bucketExists(minIOConfiguration.getCatalogueItemBucket());
            if(!isExist) {
                // Prepare Anonymous readonly Policy to fetch objects from bucket without signed url
                policyTemplate = templateEngine.getTemplate("policy-bucket-catalogueItemImage.json");
                String policy
                    = policyTemplate
                        .data("catalogueItemBucket", minIOConfiguration.getCatalogueItemBucket())
                        .render();

                minioClient.makeBucket(minIOConfiguration.getCatalogueItemBucket());
                minioClient.setBucketPolicy(minIOConfiguration.getCatalogueItemBucket(), policy);
            }
        }
        catch(Exception e) {
            throw new ImageUploadException("Error occurred while creating bucket to store catalogue item images", e);
        }
    }

    /**
     * Method to upload the image to minio object store
     * @param skuNumber
     * @param contentType
     * @param image
     * @throws ImageUploadException
     */
    public void uploadCatalogueImage(String skuNumber, String contentType, File image) throws ImageUploadException {
        try {

            if(minioClient == null)
                initializeMinIOClient();

            // Prepare options with size and content type
            PutObjectOptions options = new PutObjectOptions(image.length(),-1);
            options.setContentType(contentType);

            // Put the object to bucket
            minioClient.putObject(
                minIOConfiguration.getCatalogueItemBucket(),
                skuNumber,
                new FileInputStream(image),
                options);
        }
        catch (Exception e) {
            throw new ImageUploadException(String.format("Error occurred while uploading catalogue item image for SKU: %s", skuNumber), e);
        }
    }
}
