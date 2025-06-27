package com.digitalwallet.digitalwallet.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class AuthAspect {

    @Pointcut("@annotation(com.digitalwallet.digitalwallet.security.Authenticate)")
    public void authenticatedEndpoints() {
    }

    @Before("authenticatedEndpoints()")
    public void checkAuthentication() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest httpServletRequest = attributes.getRequest();
        String authHeader = httpServletRequest.getHeader("Authorization");
        String path = httpServletRequest.getRequestURI();
        log.info("Intercepted path: {}", path);

        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/actuator")) {
            return;
        }

//        if (authHeader == null || !authHeader.equals("TEST")) {
//            throw new InvalidUserException("Unauthorized");
//        }
    }
}
