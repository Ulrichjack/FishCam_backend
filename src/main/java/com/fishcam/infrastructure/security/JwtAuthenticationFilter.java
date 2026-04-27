package com.fishcam.infrastructure.security;

import com.fishcam.domain.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    // ✅ SUPPRIMÉ : private final UserRepository userRepository;
    // → Plus besoin ! On utilise userDetailsService qui retourne déjà User

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de token → passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ FIX 1 : Try/catch sur le parsing JWT
        // Si le token est expiré ou corrompu → on passe sans authentifier
        // au lieu de crasher avec une erreur 500
        final String jwt;
        final String userPhone;

        try {
            jwt = authHeader.substring(7);
            userPhone = jwtService.extractPhone(jwt);
        } catch (Exception e) {
            log.warn("Token JWT malformé ou expiré : {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Si phone extrait et pas déjà authentifié
        if (userPhone != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ✅ FIX 2 : UNE SEULE requête DB au lieu de DEUX
            // Ton CustomUserDetailsService retourne déjà l'entité User
            // (car User implements UserDetails)
            // Donc on cast directement → plus besoin de UserRepository ici
            final User user;
            try {
                user = (User) userDetailsService.loadUserByUsername(userPhone);
            } catch (Exception e) {
                log.warn("Utilisateur introuvable pour le phone : {}", userPhone);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.validateToken(jwt, user)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,           // Principal = User entity (pour authentication.getPrincipal())
                        null,
                        user.getAuthorities()  // ✅ Direct sur user, pas besoin de userDetails séparé
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Utilisateur authentifié: {} (ID: {})", user.getPhone(), user.getId());
            }
        }

        filterChain.doFilter(request, response);
    }
}