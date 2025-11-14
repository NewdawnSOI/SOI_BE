package com.soi.backend.domain.category.repository;

import com.soi.backend.domain.category.entity.CategoryUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryUserRepository extends JpaRepository<CategoryUser, Long> {

}
