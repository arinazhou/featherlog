package com.arinazhou.featherlog.auth;

import com.arinazhou.featherlog.auth.AuthDtos.LoginRequest;
import com.arinazhou.featherlog.auth.AuthDtos.RegisterRequest;
import com.arinazhou.featherlog.auth.AuthDtos.TokenResponse;
import com.arinazhou.featherlog.common.ConflictException;
import com.arinazhou.featherlog.user.AppUser;
import com.arinazhou.featherlog.user.UserRepository;
import java.time.Clock;
import java.util.Locale;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final Clock clock;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("An account with that email already exists");
        }
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password()),
                request.displayName().trim(), clock.instant()));
        return tokenService.issue(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        return tokenService.issue(user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
