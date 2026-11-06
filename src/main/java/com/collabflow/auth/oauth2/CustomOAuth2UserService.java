package com.collabflow.auth.oauth2;

import com.collabflow.auth.domain.User;
import com.collabflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String provider = request.getClientRegistration().getRegistrationId(); // "google"
        String email    = (String) attrs.get("email");
        String name     = (String) attrs.get("name");
        String picture  = (String) attrs.get("picture");

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateOAuthInfo(name, picture);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.createOAuth(email, name, picture, provider)
                ));

        return new OAuth2UserPrincipal(user, attrs);
    }
}
