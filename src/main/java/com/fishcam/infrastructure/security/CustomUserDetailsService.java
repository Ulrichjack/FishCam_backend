package com.fishcam.infrastructure.security;

import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé: {}", phone);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + phone);
                });

        log.debug("Utilisateur chargé: {} ({})", user.getPhone(), user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone())  // ← PHONE au lieu d'email
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}