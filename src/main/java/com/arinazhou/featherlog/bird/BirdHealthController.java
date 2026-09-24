package com.arinazhou.featherlog.bird;

import com.arinazhou.featherlog.alert.AlertResponse;
import com.arinazhou.featherlog.alert.AlertService;
import com.arinazhou.featherlog.care.CareTaskService;
import com.arinazhou.featherlog.common.CurrentUser;
import com.arinazhou.featherlog.weight.WeightAssessment;
import com.arinazhou.featherlog.weight.WeightService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** One-glance dashboard: weight trend, open alerts and overdue care for a single bird. */
@RestController
public class BirdHealthController {

    public record HealthSummary(Long birdId, String name, WeightAssessment weight, List<AlertResponse> openAlerts,
                                long overdueCareTasks) {
    }

    private final BirdService birdService;
    private final WeightService weightService;
    private final AlertService alertService;
    private final CareTaskService careTaskService;
    private final Clock clock;

    public BirdHealthController(BirdService birdService, WeightService weightService, AlertService alertService,
                                CareTaskService careTaskService, Clock clock) {
        this.birdService = birdService;
        this.weightService = weightService;
        this.alertService = alertService;
        this.careTaskService = careTaskService;
        this.clock = clock;
    }

    @GetMapping("/api/birds/{birdId}/health")
    public HealthSummary health(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        Instant now = clock.instant();
        long overdue = careTaskService.list(bird).stream().filter(t -> t.isOverdue(now)).count();
        List<AlertResponse> alerts = alertService.openForBird(bird.getId()).stream().map(AlertResponse::from).toList();
        return new HealthSummary(bird.getId(), bird.getName(), weightService.assess(bird), alerts, overdue);
    }
}
