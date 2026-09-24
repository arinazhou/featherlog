package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.bird.BirdService;
import com.arinazhou.featherlog.common.CurrentUser;
import com.arinazhou.featherlog.common.PageResponse;
import com.arinazhou.featherlog.weight.WeightDtos.WeightLogResult;
import com.arinazhou.featherlog.weight.WeightDtos.WeightRequest;
import com.arinazhou.featherlog.weight.WeightDtos.WeightResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/birds/{birdId}/weights")
public class WeightController {

    private final BirdService birdService;
    private final WeightService weightService;

    public WeightController(BirdService birdService, WeightService weightService) {
        this.birdService = birdService;
        this.weightService = weightService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeightLogResult log(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId,
                               @Valid @RequestBody WeightRequest request) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        return weightService.record(bird, request);
    }

    @GetMapping
    public PageResponse<WeightResponse> history(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId,
                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                @RequestParam(defaultValue = "30") @Min(1) @Max(200) int size) {
        Bird bird = birdService.getOwned(CurrentUser.id(jwt), birdId);
        return PageResponse.from(weightService.history(bird, PageRequest.of(page, size)), WeightResponse::from);
    }
}
