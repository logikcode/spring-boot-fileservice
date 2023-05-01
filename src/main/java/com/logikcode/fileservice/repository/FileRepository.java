package com.logikcode.fileservice.repository;

import com.logikcode.fileservice.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<UserFile, Long> {

    UserFile findByFileName(String fileName);
}
