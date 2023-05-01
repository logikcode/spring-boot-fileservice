package com.logikcode.fileservice.service;

import com.logikcode.fileservice.dto.FileDownloadResponse;
import com.logikcode.fileservice.dto.FileUploadResponse;
import com.logikcode.fileservice.dto.ProductDto;
import com.logikcode.fileservice.entity.ImageFile;
import com.logikcode.fileservice.exception.TooManyFilesException;
import com.logikcode.fileservice.model.Product;
import com.logikcode.fileservice.repository.ImageRepository;
import com.logikcode.fileservice.repository.ProductRepository;
import com.logikcode.fileservice.util.ImageUtil;
import com.logikcode.fileservice.util.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

@Service
//@RequiredArgsConstructor
@Getter
@Slf4j
public class StorageService {
    private ImageRepository fileRepository;
    @Autowired
    private  ProductRepository productRepository;
    private Path fileStoragePath;
    @Value("${file.storage.location:./src/main/product-files/")
    private static final String UPLOAD_DIR = "./src/main/product-files/";
    public StorageService(ImageRepository fileRepository, ProductRepository productRepository){
        this.fileRepository = fileRepository;
        this.productRepository = productRepository;
    }

    public StorageService(){
        fileStoragePath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
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

    public String storeFileToFileSystem(MultipartFile multipartFile, ProductDto productDto){

        Product product = new Product();
        BeanUtils.copyProperties(productDto, product);
        Product savedProduct = productRepository.save(product);

        String uploadDir = UPLOAD_DIR + savedProduct.getName();
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        Path filePath = Paths.get(UPLOAD_DIR  + savedProduct.getName());

        Path productImageUrl =  saveFileToProductDirectory(uploadDir, multipartFile, fileName);
        product.setProductImageUrl(productImageUrl);
        productRepository.save(product);

        //Path filePath = Paths.get(fileStoragePath + "/" +fileName);

        try {
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return fileName;
    }

    public List<FileUploadResponse> handleMultipleFileSaveToFileSystem(MultipartFile[] files, ProductDto productDto,
                                                                       HttpServletRequest servletRequest){
        //TODO - abstract product creation away
        Product product = new Product();
        BeanUtils.copyProperties(productDto,product);
        Product savedProduct = productRepository.save(product);

        List<FileUploadResponse> responseList = new ArrayList<>();
        String SUBDIR = String.valueOf(savedProduct.getName());

        if (files.length > 10){
            throw new TooManyFilesException("Files size exceeded");
        }
        String requestUrl = UrlUtil.getSiteUriPath(servletRequest);
        String normalizedUrl = UrlUtil.normalizedUrl(requestUrl,0,(requestUrl.indexOf("u")) - 1);

        log.info("REQUEST URL -> {}", requestUrl);
        log.info("NORMALIZED URL -> {} ", normalizedUrl);

        Arrays.stream(files).toList().forEach(file ->{
            //String fileName = storageService.storeFileToFileSystem(file);

           String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String url = UrlUtil.buildDownloadUrl(normalizedUrl,fileName,"download/");
            String fileType = file.getContentType();

            saveFileToProductDirectory(UPLOAD_DIR+SUBDIR, file, fileName);

            FileUploadResponse response = new FileUploadResponse();
            response.setFileName(fileName);
            response.setFileType(fileType);
            response.setFileUrl(url);

            responseList.add(response);
        });
        return responseList;
    }

    private Path saveFileToProductDirectory(String uploadDir, MultipartFile file, String fileName){
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
            log.info("UPLOAD PATH -> {} ", uploadDir);
            log.info("FILE PATH -> {} ", filePath);
            Files.copy(fileStream,filePath, StandardCopyOption.REPLACE_EXISTING);

        }catch (IOException io){
            log.info("EXCEPTION READING FILE INPUT STREAM");
        }
        return uploadPath;
    }

    public String storeFileToFileSystem(MultipartFile multipartFile){

//        Product product = new Product();
//        product.setId(product.getId());
//        product.setName(productDto.getName());
//        product.setPrice(productDto.getPrice());


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

    public Resource downloadImageFromFileSystem(String fileName, long productId) {
        String SUBDIR = String.valueOf(productId);
      Path path = Paths.get(UPLOAD_DIR).toAbsolutePath().resolve(fileName);
      Path dir = Paths.get(UPLOAD_DIR + SUBDIR).toAbsolutePath().resolve(fileName).normalize();
        Resource resource;
        try {
            // resource = new UrlResource(path.toUri());
             resource = new UrlResource(dir.toUri());

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

    public FileDownloadResponse getAllProductFilesInDirectory(String directory, Product product){
    Path productDirectory = Paths.get(directory + product.getName());
         List<Path> allProductFiles = getAllImages(productDirectory);

         FileDownloadResponse fileUploadResponse = FileDownloadResponse
                 .builder()
                 .productId(product.getId())
                 .productName(product.getName())
                 .productFiles(allProductFiles)
                 .build();

         return fileUploadResponse;

    }

    private List<Path> getAllImages(Path productDirectory){
        Stream<Path> filesStream = null;
        List<Path> productFiles;
        try {
            filesStream = Files.list(productDirectory);
        } catch (IOException ex){
            log.info("EXCEPTION WHILE TRANSFORMING THE PRODUCT DIRECTORY TO STREAMS");
        }
        assert filesStream != null;
        productFiles = filesStream.filter(Files::isRegularFile)
                .filter(file -> file.toString().toLowerCase().endsWith(".jpg")
                        || file.toString().toLowerCase().endsWith(".jpeg")
                        || file.toString().toLowerCase().endsWith(".png")
                        || file.toString().toLowerCase().endsWith(".gif")
                ).map(Path::normalize).toList();

        return productFiles;
    }

    public String getUPLOAD_DIR() {
        return UPLOAD_DIR;
    }
}
