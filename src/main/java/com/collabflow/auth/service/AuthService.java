package com.collabflow.auth.service;

import com.collabflow.auth.domain.User;
import com.collabflow.auth.dto.LoginRequest;
import com.collabflow.auth.dto.RegisterRequest;
import com.collabflow.auth.dto.TokenResponse;
import com.collabflow.auth.jwt.JwtTokenProvider;
import com.collabflow.auth.repository.UserRepository;
import com.collabflow.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("Email already in use: " + request.getEmail());
        }

        User user = User.createLocal(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        );
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> BusinessException.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("Invalid email or password");
        }

        return issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("Invalid refresh token");
        }

        String userId = jwtTokenProvider.getSubject(refreshToken);
        String stored = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        if (!refreshToken.equals(stored)) {
            throw BusinessException.unauthorized("Refresh token has been invalidated");
        }

        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> BusinessException.unauthorized("User not found"));

        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken)) {
            String userId = jwtTokenProvider.getSubject(refreshToken);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        }
    }

    public TokenResponse issueTokens(User user) {
        String userId = user.getId().toString();
        String accessToken = jwtTokenProvider.createAccessToken(userId, user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // refresh token을 Redis에 7일간 저장
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(3600000L)
                .build();
    }
}
