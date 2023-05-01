package com.logikcode.fileservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
}
