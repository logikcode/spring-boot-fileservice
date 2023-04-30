package com.logikcode.fileservice.repository;


import com.logikcode.fileservice.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageFile, Long> {
    Optional<ImageFile> findByFileName(String file);
}
