package com.arinazhou.featherlog.bird;

import com.arinazhou.featherlog.bird.BirdDtos.BirdRequest;
import com.arinazhou.featherlog.bird.BirdDtos.BirdResponse;
import com.arinazhou.featherlog.common.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/birds")
public class BirdController {

    private final BirdService birdService;

    public BirdController(BirdService birdService) {
        this.birdService = birdService;
    }

    @GetMapping
    public List<BirdResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return birdService.list(CurrentUser.id(jwt)).stream().map(BirdResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BirdResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BirdRequest request) {
        return BirdResponse.from(birdService.create(CurrentUser.id(jwt), request));
    }

    @GetMapping("/{birdId}")
    public BirdResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId) {
        return BirdResponse.from(birdService.getOwned(CurrentUser.id(jwt), birdId));
    }

    @PutMapping("/{birdId}")
    public BirdResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId,
                               @Valid @RequestBody BirdRequest request) {
        return BirdResponse.from(birdService.update(CurrentUser.id(jwt), birdId, request));
    }

    @DeleteMapping("/{birdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long birdId) {
        birdService.delete(CurrentUser.id(jwt), birdId);
    }
}
