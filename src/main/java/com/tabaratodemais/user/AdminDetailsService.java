package com.tabaratodemais.user;

import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.*;

@Service
public class AdminDetailsService implements UserDetailsService {
    private final AdminUserRepository users;
    public AdminDetailsService(AdminUserRepository users) { this.users = users; }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        var account = users.findByEmailIgnoreCase(email.strip().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
        return User.withUsername(account.getEmail()).password(account.getPasswordHash())
            .roles(account.getRole()).disabled(!account.getEnabled()).build();
    }
}
