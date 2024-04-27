package io.getmedusa.hydra.security;

import io.getmedusa.hydra.boot.RegistrationStarter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class HydraJWEAuthFilter extends OncePerRequestFilter {

    private final String expectedAudience;

    public HydraJWEAuthFilter(String expectedAudience) {
        this.expectedAudience = expectedAudience;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = getCookie(request.getCookies(), "HYDRA-SSO");
        if(jwt == null || jwt.isBlank()) {
            final String authHeader = request.getHeader("Authorization");
            if(authHeader != null) {
                jwt = authHeader.toString().replace("Bearer ", "");
            }
        }

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        interpretJWEAndLoginIfOK(jwt);
        filterChain.doFilter(request, response);
    }

    private void interpretJWEAndLoginIfOK(String jwe) {
        try {
            Claims payload = Jwts.parser()
                    .clockSkewSeconds(5)
                    .decryptWith(RegistrationStarter.getPrivateKey())
                    .build().parseEncryptedClaims(jwe).getPayload();
            String audience = payload.getAudience().toArray()[0].toString();

            if (expectedAudience.equals(audience)) {
                String user = payload.get("u").toString();
                Object roleObj = payload.get("r");
                List<String> roles = List.of("USER");
                if (null != roleObj) {
                    List<String> providedRoles = (List<String>) roleObj;
                    if(!providedRoles.isEmpty()) {
                        roles = providedRoles;
                    }
                }
                manualLogin(user, roles);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manualLogin(String user, List<String> roles) {
        final PreAuthenticatedAuthenticationToken t = new PreAuthenticatedAuthenticationToken(user, new SecureRandom(), buildAuthorities(roles));
        t.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(t);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(List<String> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(roles.isEmpty()) {
            return authorities;
        }
        for(String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.toUpperCase()));
        }
        return authorities;
    }

    private String getCookie(Cookie[] cookies, String s) {
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(s)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}