package com.logikcode.fileupload.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Slf4j
public class FileUploadUtil {
    public static void saveFile(String uploadDir, MultipartFile file, String fileName){

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)){
            try {

                Files.createDirectories(uploadPath);
            } catch (IOException ioException){
                log.info("Exception Encountered While Creating Directory ");
            }
        }
        // read input stream from file
        try (  InputStream fileStream = file.getInputStream()){
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(fileStream,filePath, StandardCopyOption.REPLACE_EXISTING);

        }catch (IOException io){
            log.info("EXCEPTION READING FILE INPUT STREAM");
        }
    }

    public static String buildDownloadUrl(String fileName){
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/v1/file/download/")
                .path(fileName)
                .toUriString();
        return url;
    }
}
