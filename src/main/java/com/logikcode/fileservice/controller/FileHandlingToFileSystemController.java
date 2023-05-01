package com.logikcode.fileservice.controller;

import com.logikcode.fileservice.dto.FileDownloadResponse;
import com.logikcode.fileservice.dto.FileUploadResponse;
import com.logikcode.fileservice.dto.ProductDto;
import com.logikcode.fileservice.exception.TooManyFilesException;
import com.logikcode.fileservice.model.Product;
import com.logikcode.fileservice.repository.ProductRepository;
import com.logikcode.fileservice.service.StorageService;
import com.logikcode.fileservice.util.FileUploadUtil;
import com.logikcode.fileservice.util.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/file")
public class FileHandlingToFileSystemController {
    private final StorageService storageService;
    private final ProductRepository productRepository;
    @Value("${file.storage.location:files}")
    private String fileStorageLocation = "./files";
    private final String UPLOAD_DIR = "./product-image/";
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
        String normalizedFileName = StringUtils.cleanPath(fileName);

        String uploadDir = "./product-image/" + UUID.randomUUID().toString().substring(0, 10);
        FileUploadUtil.saveFile(uploadDir, file, normalizedFileName);

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
    public ResponseEntity<?> uploadFileToSystem(@RequestParam("file") MultipartFile file,
                                                 @ModelAttribute ProductDto productDto,
                                                 HttpServletRequest request){

        String contentType = request.getContentType();
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Unsupported Media Type");
        }


        String fileName = storageService.storeFileToFileSystem(file, productDto);

        String requestUrl = UrlUtil.getSiteUriPath(request);
        String normalizedUrl = UrlUtil.normalizedUrl(requestUrl,0,requestUrl.lastIndexOf("/"));


        String url = UrlUtil.buildDownloadUrl(normalizedUrl,fileName, "download/");
        String fileType = file.getContentType();
        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setFileType(fileType);
        response.setFileUrl(url);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFileFromSystem(@PathVariable("fileName") String fileName, @RequestParam("productId") long productId, HttpServletRequest servletRequest) throws IOException {
        Product product = productRepository.findById(productId).orElseThrow();

        Resource resource = storageService.downloadImageFromFileSystem(fileName, product.getId());
        String mimeType ;
        try {
            //dynamic retrieval of mediatype
           mimeType = servletRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

       } catch (Exception ex){
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (mimeType == null){
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        log.info("MIMETYPE "+mimeType);
        MediaType contentType = MediaType.parseMediaType(mimeType); //
        log.info("CONTENT-TYPE "+ contentType);
        log.info("RESOURCE IN CONTROLLER LAYER -{} ", resource);
        HttpHeaders headers = new HttpHeaders();
        headers.add("file-name", fileName);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;File-Name="+resource.getFilename());
        //MediaType.parseMediaType(Files.probeContentType(path))
       return ResponseEntity.ok()
               .contentType( contentType)
               .headers(headers)
               .body(resource);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileUploadResponse>> handleMultipleFilesUploads(@RequestParam("files") MultipartFile[] files,
                                                              @ModelAttribute ProductDto productDto, HttpServletRequest request){
        List<FileUploadResponse> responseList =  storageService.handleMultipleFileSaveToFileSystem(files, productDto, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseList);
    }
    //downloading zip file
    @GetMapping("zipDownload/{productId}")
    public void zipDownload(@RequestParam("fileName") String[] files,long productId, HttpServletResponse response){

       // ZipOutputStream zipOutputStream;
        try{
            ZipOutputStream   zipOutputStream = new ZipOutputStream(response.getOutputStream());
            Arrays.stream(files).forEach(file->{
                Resource resource = storageService.downloadImageFromFileSystem(file, productId);
                ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(resource.getFilename()));
                try {
                    zipEntry.setSize(resource.contentLength());
                    zipOutputStream.putNextEntry(zipEntry);
                    StreamUtils.copy(resource.getInputStream(), zipOutputStream);

                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }

            });
            zipOutputStream.finish();
            zipOutputStream.close();

        } catch (IOException ioException){
            log.info("EXCEPTION ZIPPING");

        }
        response.setStatus(200);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=zipfile");
    }

    @GetMapping("/download/all/{productId}")
    public ResponseEntity<FileDownloadResponse> retrieveAllFiles(@PathVariable("productId") long id){
       String uploadDir = storageService.getUPLOAD_DIR();
       Product product = productRepository.findById(id).orElseThrow();
       FileDownloadResponse response = storageService.getAllProductFilesInDirectory(uploadDir, product);
        return ResponseEntity.ok().body(response);
    }
}
