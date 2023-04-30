package com.logikcode.fileservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "users-files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "file-name")
    private String fileName;
    @Column(name ="file-content")
    @Lob
    private byte[] docFile;
}
