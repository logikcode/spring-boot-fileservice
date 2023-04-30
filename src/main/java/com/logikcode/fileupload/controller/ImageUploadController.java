package com.logikcode.fileupload.controller;

import com.logikcode.fileupload.dto.FileUploadResponse;
import com.logikcode.fileupload.exception.TooManyFilesException;
import com.logikcode.fileupload.service.StorageService;
import com.logikcode.fileupload.util.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/file")
public class ImageUploadController {
    private final StorageService storageService;
    @PostMapping("/upload111")
    public ResponseEntity<?> uploadImage(@RequestParam("image")MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(storageService.handleFileUpload(file));
    }

    @GetMapping("/download222")
    public ResponseEntity<?> downloadImageFile(@RequestParam("name") String fileName ) throws DataFormatException, IOException {
        byte[] imageData = storageService.handleImageFileDownload(fileName);
        return ResponseEntity.status(HttpStatus.OK).
                contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }

    public static String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/imagedata";

    @PostMapping("api/v2/file")
    @ResponseBody
    public String saveFile(@RequestParam("img") MultipartFile img){
        StringBuilder fileNames = new StringBuilder();
        String fileName = "username" + extractFileExtension(img);

        Path fileNameAndPath = Paths.get(uploadDirectory, fileName);
        try {
            Files.write(fileNameAndPath, img.getBytes());
        } catch (IOException ex){
            log.info("EXCEPTION THROWN WHILE WRITING TO FILE {}", ex.getMessage() );
        }
        return "Image Saved Successfully";
    }

    private String extractFileExtension(MultipartFile file){
        String fileExtension = "username" + file.getOriginalFilename().
                substring(file.getOriginalFilename().length() - 4);
        return fileExtension;
    }

    /* Code java implementation */
    @PostMapping("/upload/v3")
    public void fileUploadHandler(@RequestParam("file") MultipartFile file){
        String fileName = file.getOriginalFilename();
        String normalizedFile = StringUtils.cleanPath(fileName);

        String uploadDir = "./product-image/" + UUID.randomUUID().toString().substring(0, 10);
        FileUploadUtil.saveFile(uploadDir, file, normalizedFile);

    }

    public void fileUploadHandlerForMultipleFiles(@RequestParam("mainImage") MultipartFile mainImage,
                                                  @RequestParam("extraImages") MultipartFile[] otherImages){
        String mainImageName = StringUtils.cleanPath(mainImage.getOriginalFilename());
        String uploadDir = "./product-images"+ "product.id";
        //TODO set the main image name to the product object
        int count = 0;
        for (MultipartFile file : otherImages){
            String otherImageName = StringUtils.cleanPath(file.getOriginalFilename());
            // TODO : call the setter to set the extra image to the product
            FileUploadUtil.saveFile(uploadDir, file, otherImageName);
            
            count++;
        }

    }

    // add resource handler to the WebMvcConfigurer
    // to expose the resource path
    public void addResourceHandler(ResourceHandlerRegistry registry){
        Path productUploadDir = Paths.get("./product-images");
        String productUploadPath = productUploadDir.toFile().getAbsolutePath();

        log.info("UPLOAD DIR {} ", productUploadDir);
        log.info("UPLOAD PATH {} ", productUploadPath);

        registry.addResourceHandler("/product-images/**").addResourceLocations("file:/" + productUploadPath + "/");

    }

    // get the product path

    public String getProductImagePath(){
        if ("product" == null || "product.id" == null){
            return null;
        }
        return "product-images/" + "image-id" +"/" + "image";
    }

    @PostMapping("/upload")
    public FileUploadResponse uploadFileToSystem(@RequestParam("file") MultipartFile file){
        String fileName = storageService.storeFileToFileSystem(file);
        String url = FileUploadUtil.buildDownloadUrl(fileName);
        String fileType = file.getContentType();
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setFileUrl(url);
        return response;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFileFromSystem(@PathVariable("fileName") String fileName, HttpServletRequest servletRequest){
       Resource resource = storageService.downloadImageFromFileSystem(fileName);
        String mimeType ;
        try {
            //dynamic retrieval of mediatype
           mimeType = servletRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

       } catch (Exception ex){
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
       //MediaType contentType = MediaType.IMAGE_JPEG;
        MediaType contentType = MediaType.parseMediaType(mimeType); //

       return ResponseEntity.ok()
               .contentType(contentType)
               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName="+resource.getFilename())
               .body(resource);
    }

    @PostMapping("/upload/multiple")
    public List<FileUploadResponse> handleMultipleFilesUploads(@RequestParam("files") MultipartFile[] files){
        List<FileUploadResponse> responseList = new ArrayList<>();
        if (files.length > 10){
            throw new TooManyFilesException("Files size exceeded");
        }
        Arrays.stream(files).toList().forEach(file ->{
            String fileName = storageService.storeFileToFileSystem(file);
            String url = FileUploadUtil.buildDownloadUrl(fileName);
            String fileType = file.getContentType();

            FileUploadResponse response = new FileUploadResponse();
            response.setFileName(fileName);
            response.setFileType(fileType);
            response.setFileUrl(url);

            responseList.add(response);
        });
        return responseList;
    }


}
