package com.logikcode.fileservice.controller;

import com.logikcode.fileservice.dto.FileUploadResponse;
import com.logikcode.fileservice.model.UserFile;
import com.logikcode.fileservice.repository.FileRepository;
import com.logikcode.fileservice.util.UrlUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v2/file")
@RequiredArgsConstructor
@Slf4j
public class FileHandlingWithDBController {
    private final FileRepository fileRepository;
    //Dynamically creating an upload directory
    public static final String UPLOAD_DIR = System.getProperty("user.home"+ "/Downloads/uploads");
    @PostMapping("/upload")
    FileUploadResponse handleFileUploadToDB(@RequestParam("file")MultipartFile file, HttpServletRequest request ) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        UserFile userFile = new UserFile();
        userFile.setFileName(fileName);
        userFile.setDocFile(file.getBytes());
        fileRepository.save(userFile);

        log.info("UPLOAD DIR {} ", System.getProperty("user.home"+ "/Downloads/"));

        String requestUrl = UrlUtil.getSiteUriPath(request);
        String normalizedUrl = UrlUtil.normalizedUrl(requestUrl,0,requestUrl.lastIndexOf("/"));

        String url = UrlUtil.buildDownloadUrl(normalizedUrl,fileName,"download/");
        String contentType = file.getContentType();
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(url)
                .fileType(contentType)
                .fileName(fileName)
                .fileType(file.getContentType())
                .build();
        return response;


    }

    @GetMapping("/download/{fileName}")
    ResponseEntity<byte[]> downloadFileFromDB(@PathVariable("fileName") String fileName, HttpServletRequest request) {
        UserFile userFile = fileRepository.findByFileName(fileName);

        ServletContext servletContext = request.getServletContext();
        String mimeType = servletContext.getMimeType(userFile.getFileName());
        log.info("MIMETYPE FROM FILE OBJECT {}", MediaType.parseMediaType(mimeType));
        if (mimeType == null){
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        log.info("MIMETYPE ->{} ", MediaType.parseMediaType(mimeType));
       //byte[] decodedBytes = Base64.getDecoder().decode(userFile.getDocFile());
        return ResponseEntity.ok()
               // .contentType(MediaType.IMAGE_JPEG)
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline;fileName="+userFile.getFileName())
                .body(userFile.getDocFile());

    }
}
