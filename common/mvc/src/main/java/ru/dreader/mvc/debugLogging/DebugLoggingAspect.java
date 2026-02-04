package ru.dreader.mvc.debugLogging;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@Log4j2
public class DebugLoggingAspect {

    @Around("@annotation(DebugLog)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();
        Instant startTs = Instant.now();

        log.debug("▶ START {} at {}", method, startTs);

        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            Instant endTs = Instant.now();

            log.debug("⏹ END {} at {}, duration={} ms", method, endTs, duration);
        }
    }
}
