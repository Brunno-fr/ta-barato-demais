package com.tabaratodemais.user;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirstAdminService {
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;

    public FirstAdminService(AdminUserRepository users, PasswordEncoder encoder, JdbcTemplate jdbc) {
        this.users = users; this.encoder = encoder; this.jdbc = jdbc;
    }

    @Transactional
    public void create(String email, char[] password) {
        String normalized = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Informe um e-mail válido.");
        if (password == null || password.length < 12
                || StandardCharsets.UTF_8.encode(CharBuffer.wrap(password)).remaining() > 72
                || CharBuffer.wrap(password).chars().allMatch(Character::isWhitespace))
            throw new IllegalArgumentException("Use uma senha de pelo menos 12 caracteres e no máximo 72 bytes UTF-8.");
        // Serializa execuções concorrentes do comando; nenhuma conta existente é sobrescrita.
        jdbc.execute("SELECT pg_advisory_xact_lock(84201030)");
        if (users.count() != 0)
            throw new IllegalStateException("Já existe uma conta. Este comando cria somente o primeiro administrador.");
        users.saveAndFlush(new AdminUser(normalized, encoder.encode(CharBuffer.wrap(password)), true));
    }
}
