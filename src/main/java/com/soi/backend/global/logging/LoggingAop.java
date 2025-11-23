package com.soi.backend.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.soi.backend.global.logging.RequestIdFilter.REQUEST_ID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Aspect
@Component
public class LoggingAop {

    private final HttpServletRequest req;

    public LoggingAop(HttpServletRequest req) {
        this.req = req;
    }

    /**
     * Controller + Service만 로깅
     */
    @Pointcut("execution(* com.soi.backend..controller..*(..)) || execution(* com.soi.backend..service..*(..))")
    public void appLayer() {}

    @Before("appLayer()")
    public void logBefore(JoinPoint jp) {

        String requestId = "no-req";
        try {
            Object reqId = RequestContextHolder.currentRequestAttributes()
                    .getAttribute(REQUEST_ID, RequestAttributes.SCOPE_REQUEST);

            if (reqId != null) {
                requestId = reqId.toString();
            }
        } catch (IllegalStateException ignored) {
            // 요청 외부(스케줄러 등)
        }

        // args 길어지면 일부만 출력
        String argsString = Arrays.toString(jp.getArgs());
        if (argsString.length() > 200) {
            argsString = argsString.substring(0, 200) + "...";
        }

        log.info("[{}] CALL {}.{} args={}",
                requestId,
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                argsString
        );
    }

    /**
     * EXCEPTION은 AOP에서는 남기지 않음 (중복 방지)
     * → GlobalExceptionHandler에서만 출력
     */
    @AfterThrowing(pointcut = "appLayer()", throwing = "ex")
    public void logException(JoinPoint jp, Throwable ex) {
        // 필요 시 debug로 남길 수 있음
        log.debug("[{}] DEBUG-EX {}.{}: {}",
                req.getAttribute(REQUEST_ID),
                jp.getSignature().getDeclaringTypeName(),
                jp.getSignature().getName(),
                ex.getMessage()
        );
    }
}