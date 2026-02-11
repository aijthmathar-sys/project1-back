package com.example.project1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;


@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtFilter.class.getName());

    @Autowired
    private JwUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        filterChain.doFilter(request, response);
        return;
    }

    // ✅ 2️⃣ Public auth paths skip
    String path = request.getServletPath();
    if ("Options".equalsIgnoreCase(request.getMethod())|| path.startsWith("/auth") || path.startsWith("/api/auth")) {
        filterChain.doFilter(request, response);
        return;
    }
        
    

        String header = request.getHeader("Authorization");
        logger.info("Authorization header: " + header);

        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            logger.info("Token from header: " + token.substring(0, Math.min(20, token.length())) + "...");
        } else if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("token".equals(c.getName())) {
                    token = c.getValue();
                    logger.info("Token from cookie: " + token.substring(0, Math.min(20, token.length())) + "...");
                    break;
                }
            }
        }

        if (token != null) {
            try {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                logger.info("Extracted email: " + email + ", role: " + role);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("Authentication set for: " + email + " with roles: " + authorities);
                }
            } catch (Exception ex) {
                logger.warning("Token extraction failed: " + ex.getMessage());
            }
        } else {
            logger.warning("No token found in request");
        }

        filterChain.doFilter(request, response);
    }
}
