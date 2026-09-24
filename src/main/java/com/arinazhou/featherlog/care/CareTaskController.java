package com.arinazhou.featherlog.care;

import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.bird.BirdService;
import com.arinazhou.featherlog.care.CareTaskDtos.CareTaskRequest;
import com.arinazhou.featherlog.care.CareTaskDtos.CareTaskResponse;
import com.arinazhou.featherlog.common.CurrentUser;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/birds/{birdId}/care-tasks")
public class CareTaskController {

    private final BirdService birdService;
    private final CareTaskService careTaskService;
    private final Clock clock;

    public CareTaskController(BirdService birdService, CareTaskService careTaskService, Clock clock) {
        this.birdService = birdService;
        this.careTaskService = careTaskService;
        this.clock = clock;
    }

    @GetMapping
    public List<CareTaskResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        return careTaskService.list(bird).stream().map(t -> CareTaskResponse.from(t, clock.instant())).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CareTaskResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId,
                                   @Valid @RequestBody CareTaskRequest request) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        return CareTaskResponse.from(careTaskService.create(bird, request), clock.instant());
    }

    @PostMapping("/{taskId}/complete")
    public CareTaskResponse complete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId,
                                     @PathVariable Long taskId) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        return CareTaskResponse.from(careTaskService.complete(bird, taskId), clock.instant());
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId, @PathVariable Long taskId) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        careTaskService.deactivate(bird, taskId);
    }
}
