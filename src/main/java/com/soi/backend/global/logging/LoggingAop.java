package com.soi.backend.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import static com.soi.backend.global.logging.RequestIdFilter.REQUEST_ID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Aspect
@Component
public class LoggingAop {

    private final HttpServletRequest req;
    private final RequestUserLogResolver requestUserLogResolver;

    public LoggingAop(HttpServletRequest req, RequestUserLogResolver requestUserLogResolver) {
        this.req = req;
        this.requestUserLogResolver = requestUserLogResolver;
    }

    /**
     * Controller 요청만 로깅
     */
    @Pointcut("execution(* com.soi.backend..controller..*(..))")
    public void controllerLayer() {}

    @Before("controllerLayer()")
    public void logBefore(JoinPoint jp) {
        String httpMethod = req.getMethod();
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return;
        }

        log.info("[{}] {} {} user={} handler={}.{}",
                resolveRequestId(),
                httpMethod,
                resolveRequestUri(),
                requestUserLogResolver.resolveCurrentUserLabel(),
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName()
        );
    }

    /**
     * EXCEPTION은 AOP에서는 남기지 않음 (중복 방지)
     * → GlobalExceptionHandler에서만 출력
     */
    @AfterThrowing(pointcut = "controllerLayer()", throwing = "ex")
    public void logException(JoinPoint jp, Throwable ex) {
        // 필요 시 debug로 남길 수 있음
        log.debug("[{}] DEBUG-EX {}.{}: {}",
                req.getAttribute(REQUEST_ID),
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                ex.getMessage()
        );
    }

    private String resolveRequestId() {
        try {
            Object reqId = RequestContextHolder.currentRequestAttributes()
                    .getAttribute(REQUEST_ID, RequestAttributes.SCOPE_REQUEST);

            if (reqId != null) {
                return reqId.toString();
            }
        } catch (IllegalStateException ignored) {
            return "no-req";
        }

        return "no-req";
    }

    private String resolveRequestUri() {
        String queryString = req.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return req.getRequestURI();
        }

        return req.getRequestURI() + "?" + queryString;
    }
}
