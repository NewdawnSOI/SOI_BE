package com.soi.backend.domain.media.repository;

import com.soi.backend.domain.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByMediaKey(String mediaKey);
}
