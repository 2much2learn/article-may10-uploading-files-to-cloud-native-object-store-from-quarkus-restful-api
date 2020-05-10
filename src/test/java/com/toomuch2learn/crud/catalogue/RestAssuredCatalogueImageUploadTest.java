package com.toomuch2learn.crud.catalogue;

import com.toomuch2learn.crud.catalogue.config.MinIOTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(MinIOTestResource.class)
@DisabledOnNativeImage
public class RestAssuredCatalogueImageUploadTest extends BaseTest {

    @BeforeEach
    public void setURL() {
        RestAssured.baseURI = "http://[::1]:8081/api/v1";
    }

    @Test
    @DisplayName("Test upload Catalogue Item image")
    public void test_uploadCatalogueItemImage() {
        try {
            String skuNumber = prepareRandomSKUNumber();

            // Create Catalogue Item
            postCreateCatalogueItem(prepareCatalogueItem(skuNumber))
            .then()
                .assertThat().spec(prepareResponseSpec(201))
            .and()
                .assertThat().body("id", greaterThan(0));

            // Get Catalogue Item
            given()
                .pathParam("sku", skuNumber)
            .when()
                .get("/{sku}")
            .then()
                .assertThat().spec(prepareResponseSpec(200));

            // Load file from resource to input stream
            InputStream image = getClass().getClassLoader().getResourceAsStream("crud.png");

            // Post image upload for specific SKU
            given()
                .contentType("application/octet-stream")
                .body(image)
                .pathParam("sku", skuNumber)
            .when()
                .post("/{sku}/image")
            .then()
                .assertThat().spec(prepareResponseSpec(Response.Status.CREATED.getStatusCode()));
        }
        catch(Exception e) {
            fail("Error occurred while uploading catalogue item image endpoint", e);
        }
    }

    @Test
    @DisplayName("Test upload Catalogue Item image for invalid sku")
    public void test_uploadCatalogueItemImageForInvalidSKU() {
        try {
            String skuNumber = prepareRandomSKUNumber();

            InputStream image = getClass().getClassLoader().getResourceAsStream("crud.png");

            given()
                .contentType("application/octet-stream")
                .body(image)
                .pathParam("sku", skuNumber)
            .when()
                .post("/{sku}/image")
            .then()
                .assertThat().spec(prepareResponseSpec(Response.Status.NOT_FOUND.getStatusCode()));
        }
        catch (Exception e) {
            fail("Error occurred while testing catalogue item upload for invalid sku", e);
        }
    }

    @Test
    @DisplayName("Test upload Catalogue Item for invalid file format")
    public void test_uploadCatalogueItemForInvalidFileFormat() {
        try {
            String skuNumber = prepareRandomSKUNumber();
            postCreateCatalogueItem(prepareCatalogueItem(skuNumber))
            .then()
                .assertThat().spec(prepareResponseSpec(201))
            .and()
                .assertThat().body("id", greaterThan(0));

            InputStream image = getClass().getClassLoader().getResourceAsStream("dummy.pdf");

            given()
                .contentType("application/octet-stream")
                .body(image)
                .pathParam("sku", skuNumber)
            .when()
                .post("/{sku}/image")
            .then()
                 .assertThat().spec(prepareResponseSpec(Response.Status.BAD_REQUEST.getStatusCode()));
        }
        catch (Exception e) {
            fail("Error occurred while testing catalogue item upload for invalid image format", e);
        }
    }
}