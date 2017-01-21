package org.generationinitiative.uploader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
    String token;
    String bucket;
    String key;
    String body;
}
