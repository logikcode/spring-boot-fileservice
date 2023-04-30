package com.logikcode.fileservice.service;

import com.logikcode.fileservice.entity.ImageFile;
import com.logikcode.fileservice.repository.ImageRepository;
import com.logikcode.fileservice.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;

@Service
//@RequiredArgsConstructor

@Slf4j
public class StorageService {
    private ImageRepository fileRepository;
    private Path fileStoragePath;
    @Value("${file.storage.location:files}")
    private String fileStorageLocation = "./files";
    public StorageService(ImageRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public StorageService(){
    fileStoragePath = Paths.get(fileStorageLocation).toAbsolutePath().normalize();
    log.info("FILE STORAGE PATH {}", fileStoragePath);

    try {
        Files.createDirectories(fileStoragePath);
    } catch (IOException ioException){
        log.info("EXCEPTION THROWN WHILE CREATING FILE DIRECTORY ->{} ", ioException.getMessage());

    }
    }
    public String handleFileUpload(MultipartFile file) throws IOException {
        ImageFile image = ImageFile.builder()
                .fileName(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(ImageUtil.compressImage(file.getBytes()))
                .build();
      ImageFile savedImage =  fileRepository.save(image);
        return "File successfully saved " + file.getOriginalFilename();
    }

    public byte[] handleImageFileDownload(String fileName) throws DataFormatException, IOException {
        ImageFile file = fileRepository.findByFileName(fileName).orElseThrow();
        System.out.println("file "+ Arrays.toString(file.getImageData()));
        byte[] image = ImageUtil.decompressImage(file.getImageData());
        return image;
    }

    public String storeFileToFileSystem(MultipartFile multipartFile){
       String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
       log.info("CLEANSE FILE NAME -> {}", fileName);
       Path filePath = Paths.get(fileStoragePath + "/" +fileName);
       log.info("PATH NAME AND FILE NAME -> {}",filePath);
        try {
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return fileName;
    }


    public Resource downloadImageFromFileSystem(String fileName) {
      Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(fileName);
        Resource resource;
        try {
             resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (resource.exists() && resource.isReadable()){
            log.info("RESOURCE OBJECT ... -> {}", resource);
            return resource;
        } else {
            throw new RuntimeException("The File does not exist or is not readable");

        }
    }
}
