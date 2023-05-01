package com.logikcode.fileservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Data
@Builder

public class FileDownloadResponse {
   private long productId;
   private String productName;
   private List<Path> productFiles;
}
