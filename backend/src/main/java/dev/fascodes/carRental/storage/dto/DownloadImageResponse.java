package dev.fascodes.carRental.storage.dto;

import org.springframework.core.io.InputStreamResource;

public class DownloadImageResponse {
    private InputStreamResource resource;
    private String contentType;
    private String originalFilename;

    public DownloadImageResponse(InputStreamResource resource, String contentType, String originalFilename) {
        this.resource = resource;
        this.contentType = contentType;
        this.originalFilename = originalFilename;
    }
}
