package com.unza.ubirr.security;

import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final AppUserRepository appUserRepository;
  private final TokenRevocationService tokenRevocationService;

  public JwtAuthenticationFilter(
    JwtService jwtService,
    AppUserRepository appUserRepository,
    TokenRevocationService tokenRevocationService
  ) {
    this.jwtService = jwtService;
    this.appUserRepository = appUserRepository;
    this.tokenRevocationService = tokenRevocationService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);
    if (!jwtService.isTokenValid(token) || SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String jti = jwtService.extractJti(token);
    if (jti != null && tokenRevocationService.isRevoked(jti)) {
      filterChain.doFilter(request, response);
      return;
    }

    appUserRepository.findByIdWithRoles(jwtService.extractUserId(token)).ifPresent(user -> {
      // Single-session enforcement: reject if another device has logged in since this token was issued
      String tokenSid = jwtService.extractSessionId(token);
      if (tokenSid != null && user.currentSessionId != null && !tokenSid.equals(user.currentSessionId)) {
        return; // Session superseded — do not authenticate; caller receives 401
      }
      UserPrincipal principal = UserPrincipal.from(user);
      UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    });

    filterChain.doFilter(request, response);
  }
}
