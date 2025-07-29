package com.enterprise.s3browser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Data Transfer Object for S3 objects exposed via REST API.
 */
@Schema(description = "S3 object information")
public class S3ObjectDto {
    
    @Schema(description = "Object key/path", example = "documents/file.pdf")
    @JsonProperty("key")
    private String key;

    @Schema(description = "Object ETag", example = "d41d8cd98f00b204e9800998ecf8427e")
    @JsonProperty("etag")
    private String etag;

    @Schema(description = "Object size in bytes", example = "1024")
    @JsonProperty("size")
    private long size;

    @Schema(description = "Last modified timestamp")
    @JsonProperty("lastModified")
    private Instant lastModified;

    @Schema(description = "Storage class", example = "STANDARD")
    @JsonProperty("storageClass")
    private String storageClass;

    @Schema(description = "Whether this is a directory/folder", example = "false")
    @JsonProperty("isDirectory")
    private boolean isDirectory;

    public S3ObjectDto() {}

    public S3ObjectDto(String key, String etag, long size, Instant lastModified, 
                      String storageClass, boolean isDirectory) {
        this.key = key;
        this.etag = etag;
        this.size = size;
        this.lastModified = lastModified;
        this.storageClass = storageClass;
        this.isDirectory = isDirectory;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}