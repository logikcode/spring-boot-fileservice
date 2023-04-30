package com.logikcode.fileservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FileUploadResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
}
