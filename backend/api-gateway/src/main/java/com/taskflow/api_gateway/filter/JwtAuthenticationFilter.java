package com.taskflow.apigateway.filter;

import com.taskflow.apigateway.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RouteValidator routeValidator;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            RouteValidator routeValidator) {

        this.jwtService = jwtService;
        this.routeValidator = routeValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ==========================================
        // 1. PUBLIC ENDPOINT
        // ==========================================

        if (!routeValidator.isSecured.test(path)) {

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 2. GET JWT FROM AUTHORIZATION HEADER
        // ==========================================

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter()
                    .write("Missing Authorization Header");

            return;
        }

        String token = authHeader.substring(7);

        // ==========================================
        // 3. VALIDATE JWT
        // ==========================================

        if (!jwtService.validateToken(token)) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter()
                    .write("Invalid JWT Token");

            return;
        }

        // ==========================================
        // 4. EXTRACT USER INFORMATION
        // ==========================================

        String email = jwtService.extractEmail(token);

        String role = jwtService.extractRole(token);

        System.out.println(
                "Authenticated User: " + email);

        System.out.println(
                "Authenticated Role: " + role);

        // ==========================================
        // 5. CREATE SPRING SECURITY AUTHENTICATION
        // ==========================================

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        )
                );

        // ==========================================
        // 6. STORE AUTHENTICATION IN SECURITY CONTEXT
        // ==========================================

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        // ==========================================
        // 7. ADMIN-ONLY ENDPOINTS
        // ==========================================

        //boolean adminOnly =
                //isAdminOnlyEndpoint(path, method);

        //if (adminOnly &&
                //!"ADMIN".equalsIgnoreCase(role)) {

            //response.setStatus(
                    //HttpServletResponse.SC_FORBIDDEN);

            //response.getWriter()
                    //.write(
                            //"Access Denied: ADMIN role required"
                    //);

            //return;
        //}

        // ==========================================
        // 8. REQUEST AUTHORIZED
        // ==========================================

        filterChain.doFilter(request, response);
    }

//    private boolean isAdminOnlyEndpoint(
//            String path,
//            String method) {
//
//        // DELETE /api/tasks/**
//        if (path.startsWith("/api/tasks/")
//                && "DELETE".equalsIgnoreCase(method)) {
//
//            return true;
//        }
//
//        // DELETE /api/projects/**
//        if (path.startsWith("/api/projects/")
//                && "DELETE".equalsIgnoreCase(method)) {
//
//            return true;
//        }
//
//        return false;
//    }
}