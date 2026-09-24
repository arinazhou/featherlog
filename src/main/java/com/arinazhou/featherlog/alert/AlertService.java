package com.arinazhou.featherlog.alert;

import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.care.CareTask;
import com.arinazhou.featherlog.common.NotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final HealthAlertRepository alerts;
    private final Clock clock;

    public AlertService(HealthAlertRepository alerts, Clock clock) {
        this.alerts = alerts;
        this.clock = clock;
    }

    /**
     * Opens a bird-level alert unless one of the same type is already open, so repeated weigh-ins
     * during a bad week produce one alert rather than a flood of duplicates.
     */
    @Transactional
    public Optional<HealthAlert> raise(Bird bird, AlertType type, Severity severity, String message) {
        if (alerts.existsByBirdIdAndTypeAndResolvedAtIsNull(bird.getId(), type)) {
            return Optional.empty();
        }
        return Optional.of(alerts.save(new HealthAlert(bird, null, type, severity, message, clock.instant())));
    }

    /** Opens a CARE_OVERDUE alert for the task unless one is already open for it. */
    @Transactional
    public Optional<HealthAlert> raiseOverdue(CareTask task, String message) {
        if (alerts.existsByCareTaskIdAndResolvedAtIsNull(task.getId())) {
            return Optional.empty();
        }
        return Optional.of(alerts.save(new HealthAlert(task.getBird(), task, AlertType.CARE_OVERDUE,
                Severity.WARNING, message, clock.instant())));
    }

    @Transactional
    public void resolveForTask(Long careTaskId) {
        alerts.findByCareTaskIdAndResolvedAtIsNull(careTaskId).forEach(a -> a.resolve(clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<HealthAlert> list(Long ownerId, boolean openOnly) {
        return openOnly
                ? alerts.findByBirdOwnerIdAndResolvedAtIsNullOrderByCreatedAtDesc(ownerId)
                : alerts.findByBirdOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public List<HealthAlert> openForBird(Long birdId) {
        return alerts.findByBirdIdAndResolvedAtIsNullOrderByCreatedAtDesc(birdId);
    }

    @Transactional
    public HealthAlert resolve(Long ownerId, Long alertId) {
        HealthAlert alert = alerts.findByIdAndBirdOwnerId(alertId, ownerId)
                .orElseThrow(() -> new NotFoundException("Alert", alertId));
        alert.resolve(clock.instant());
        return alert;
    }
}
