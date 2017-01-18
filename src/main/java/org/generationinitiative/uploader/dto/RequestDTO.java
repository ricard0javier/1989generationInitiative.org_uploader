package org.generationinitiative.uploader.dto;

import lombok.Data;

@Data
public class RequestDTO {
    private String token;
    private String bucket;
    private String key;
    private String body;
}
