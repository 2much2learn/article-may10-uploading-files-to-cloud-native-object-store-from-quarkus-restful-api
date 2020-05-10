package com.toomuch2learn.crud.catalogue.config;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;

public class MinIOTestResource implements QuarkusTestResourceLifecycleManager {

    public GenericContainer minio;

    private final String ACCESS_SECRET_KEY = "minioadmin";
    private final int EXPOSED_PORT = 9000;

    @Override
    public Map<String, String> start() {
        minio
            = new GenericContainer<>("minio/minio")
                .withExposedPorts(EXPOSED_PORT, EXPOSED_PORT)
                .withEnv("MINIO_ACCESS_KEY", ACCESS_SECRET_KEY)
                .withEnv("MINIO_SECRET_KEY", ACCESS_SECRET_KEY)
                .withCommand("server", "/data")
                .waitingFor(Wait.forHttp("/minio/health/ready"))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(MinIOTestResource.class)));

        minio.start();

        // Map environment variables with the started container's exposed port
        Map<String, String> map = new HashMap<>();
        map.put("minio.port", Integer.toString(minio.getFirstMappedPort()));
        map.put("minio.access-key", ACCESS_SECRET_KEY);
        map.put("minio.secret-key", ACCESS_SECRET_KEY);
        return map;
    }

    @Override
    public void stop() {
        minio.stop();
    }
}
