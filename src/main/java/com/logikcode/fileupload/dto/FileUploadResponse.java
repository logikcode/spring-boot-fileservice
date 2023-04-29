package com.logikcode.fileupload.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FileUploadResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
}
