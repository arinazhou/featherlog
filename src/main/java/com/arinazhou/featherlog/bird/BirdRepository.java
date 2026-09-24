package com.arinazhou.featherlog.bird;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BirdRepository extends JpaRepository<Bird, Long> {

    List<Bird> findByOwnerIdOrderByNameAsc(Long ownerId);

    Optional<Bird> findByIdAndOwnerId(Long id, Long ownerId);
}
