package com.logikcode.fileservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "files")
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "filename")
    private String fileName;
    private String fileType;
    @Column(name ="content")
    @Lob
    private byte[] docFile;

    public byte[] getDocFile() {
        return docFile;
    }


}
