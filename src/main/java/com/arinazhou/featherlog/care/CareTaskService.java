package com.arinazhou.featherlog.care;

import com.arinazhou.featherlog.alert.AlertService;
import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.care.CareTaskDtos.CareTaskRequest;
import com.arinazhou.featherlog.common.NotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareTaskService {

    private static final EnumSet<CareTaskType> DEFAULT_SCHEDULE = EnumSet.of(
            CareTaskType.FRESH_WATER, CareTaskType.FRESH_FOOD, CareTaskType.FRESH_GREENS, CareTaskType.CAGE_CLEAN,
            CareTaskType.CUTTLEBONE_CHECK, CareTaskType.NAIL_BEAK_CHECK, CareTaskType.VET_CHECKUP);

    private final CareTaskRepository tasks;
    private final AlertService alertService;
    private final Clock clock;

    public CareTaskService(CareTaskRepository tasks, AlertService alertService, Clock clock) {
        this.tasks = tasks;
        this.alertService = alertService;
        this.clock = clock;
    }

    /** Gives every new bird a standard budgie care schedule that the owner can then tweak. */
    @Transactional
    public void seedDefaults(Bird bird) {
        Instant now = clock.instant();
        tasks.saveAll(DEFAULT_SCHEDULE.stream()
                .map(type -> new CareTask(bird, type, type.defaultTitle(), type.defaultIntervalDays(), now))
                .toList());
    }

    @Transactional(readOnly = true)
    public List<CareTask> list(Bird bird) {
        return tasks.findByBirdIdAndActiveTrueOrderByNextDueAtAsc(bird.getId());
    }

    @Transactional
    public CareTask create(Bird bird, CareTaskRequest request) {
        CareTaskType type = request.type();
        String title = request.title() == null || request.title().isBlank() ? type.defaultTitle() : request.title().trim();
        int interval = request.intervalDays() == null ? type.defaultIntervalDays() : request.intervalDays();
        return tasks.save(new CareTask(bird, type, title, interval, clock.instant()));
    }

    @Transactional
    public CareTask complete(Bird bird, Long taskId) {
        CareTask task = get(bird, taskId);
        task.complete(clock.instant());
        alertService.resolveForTask(task.getId());
        return task;
    }

    @Transactional
    public void deactivate(Bird bird, Long taskId) {
        CareTask task = get(bird, taskId);
        task.deactivate();
        alertService.resolveForTask(task.getId());
    }

    /**
     * Opens one CARE_OVERDUE alert per overdue task. Safe to run repeatedly: tasks that already have an
     * open alert are skipped, and completing a task resolves its alert.
     *
     * @return number of alerts opened
     */
    @Transactional
    public int raiseOverdueAlerts() {
        Instant now = clock.instant();
        int opened = 0;
        for (CareTask task : tasks.findOverdue(now)) {
            long hoursLate = Duration.between(task.getNextDueAt(), now).toHours();
            String message = String.format("%s for %s is overdue by %d hour(s)",
                    task.getTitle(), task.getBird().getName(), hoursLate);
            if (alertService.raiseOverdue(task, message).isPresent()) {
                opened++;
            }
        }
        return opened;
    }

    private CareTask get(Bird bird, Long taskId) {
        return tasks.findByIdAndBirdIdAndActiveTrue(taskId, bird.getId())
                .orElseThrow(() -> new NotFoundException("Care task", taskId));
    }
}
