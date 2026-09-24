package com.arinazhou.featherlog.alert;

import com.arinazhou.featherlog.common.CurrentUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> list(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(defaultValue = "true") boolean open) {
        return alertService.list(CurrentUser.id(jwt), open).stream().map(AlertResponse::from).toList();
    }

    @PostMapping("/{alertId}/resolve")
    public AlertResponse resolve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long alertId) {
        return AlertResponse.from(alertService.resolve(CurrentUser.id(jwt), alertId));
    }
}
