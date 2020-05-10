package com.toomuch2learn.crud.catalogue.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigProperties(prefix = "minio")
public class MinIOConfiguration {

    private boolean useSsl;
    private String host;
    private int port;
    private String accessKey;
    private String secretKey;
    private String catalogueItemBucket;
}
