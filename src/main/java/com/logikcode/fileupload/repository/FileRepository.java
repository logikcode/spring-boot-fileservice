package com.logikcode.fileupload.repository;


import com.logikcode.fileupload.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<ImageFile, Long> {
    Optional<ImageFile> findByFileName(String file);
}
