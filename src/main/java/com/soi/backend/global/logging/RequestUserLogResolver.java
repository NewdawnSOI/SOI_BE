package com.soi.backend.global.logging;

import com.soi.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
@RequiredArgsConstructor
public class RequestUserLogResolver {

    private static final String REQUEST_USER_LABEL = "requestUserLabel";

    private final UserRepository userRepository;

    public String resolveCurrentUserLabel() {
        try {
            RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
            Object cached = attributes.getAttribute(REQUEST_USER_LABEL, RequestAttributes.SCOPE_REQUEST);
            if (cached instanceof String label) {
                return label;
            }

            String resolved = resolveFromSecurityContext();
            attributes.setAttribute(REQUEST_USER_LABEL, resolved, RequestAttributes.SCOPE_REQUEST);
            return resolved;
        } catch (IllegalStateException ignored) {
            return "system";
        }
    }

    private String resolveFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        Long userId = extractUserId(authentication.getPrincipal());
        if (userId == null) {
            return "anonymous";
        }

        return userRepository.findById(userId)
                .map(user -> String.format("%s(id=%d, nickname=%s)", user.getName(), user.getId(), user.getNickname()))
                .orElse("unknown(id=" + userId + ")");
    }

    private Long extractUserId(Object principal) {
        if (principal instanceof Long userId) {
            return userId;
        }

        if (principal instanceof String principalString) {
            if ("anonymousUser".equals(principalString)) {
                return null;
            }

            try {
                return Long.parseLong(principalString);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }
}
