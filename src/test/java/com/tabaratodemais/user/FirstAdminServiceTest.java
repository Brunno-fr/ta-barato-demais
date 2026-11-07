package com.tabaratodemais.user;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FirstAdminServiceTest {
    private final AdminUserRepository users = mock(AdminUserRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final FirstAdminService service = new FirstAdminService(users, encoder, jdbc);

    @Test void storesOnlyHashAndNormalizesEmail() {
        service.create(" Owner@Example.test ", "Synthetic-test-password-123".toCharArray());
        var saved = ArgumentCaptor.forClass(AdminUser.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("owner@example.test");
        assertThat(saved.getValue().getEnabled()).isTrue();
        assertThat(saved.getValue().getPasswordHash()).startsWith("$2");
        assertThat(encoder.matches("Synthetic-test-password-123", saved.getValue().getPasswordHash())).isTrue();
        var order = inOrder(jdbc, users);
        order.verify(jdbc).execute("SELECT pg_advisory_xact_lock(84201030)");
        order.verify(users).count();
    }
    @Test void refusesToOverwriteExistingAccounts() {
        when(users.count()).thenReturn(1L);
        assertThatThrownBy(() -> service.create("owner@example.test", "Synthetic-test-password-123".toCharArray()))
            .isInstanceOf(IllegalStateException.class);
        verify(users, never()).saveAndFlush(any());
    }
    @Test void rejectsInvalidEmailShortAndOversizedUtf8Passwords() {
        assertThatThrownBy(() -> service.create("invalid", "Synthetic-test-password-123".toCharArray()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create("owner@example.test", "short".toCharArray()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create("owner@example.test", "á".repeat(37).toCharArray()))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(users, jdbc);
    }
}
