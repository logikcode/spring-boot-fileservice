package com.logikcode.fileservice.repository;

import com.logikcode.fileservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
