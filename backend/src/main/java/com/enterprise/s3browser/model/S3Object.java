package com.enterprise.s3browser.model;

import java.time.Instant;

/**
 * Domain model representing an S3 object.
 */
public class S3Object {
    private String key;
    private String etag;
    private long size;
    private Instant lastModified;
    private String storageClass;
    private boolean isDirectory;

    public S3Object() {}

    public S3Object(String key, String etag, long size, Instant lastModified, String storageClass, boolean isDirectory) {
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