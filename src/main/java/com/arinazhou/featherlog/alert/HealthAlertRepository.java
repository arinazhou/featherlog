package com.arinazhou.featherlog.alert;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {

    boolean existsByBirdIdAndTypeAndResolvedAtIsNull(Long birdId, AlertType type);

    boolean existsByCareTaskIdAndResolvedAtIsNull(Long careTaskId);

    List<HealthAlert> findByCareTaskIdAndResolvedAtIsNull(Long careTaskId);

    List<HealthAlert> findByBirdOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<HealthAlert> findByBirdOwnerIdAndResolvedAtIsNullOrderByCreatedAtDesc(Long ownerId);

    List<HealthAlert> findByBirdIdAndResolvedAtIsNullOrderByCreatedAtDesc(Long birdId);

    Optional<HealthAlert> findByIdAndBirdOwnerId(Long id, Long ownerId);
}
