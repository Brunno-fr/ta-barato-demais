package com.tabaratodemais.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bootstrap-admin")
@Transactional
class AdminSetupIT {
    @Autowired FirstAdminService setup;
    @Autowired AdminUserRepository users;

    @Test void setupUsesDatabaseLockAndNeverReplacesAnAccount() {
        long before = users.count();
        char[] password = "Synthetic-setup-only-123".toCharArray();
        if (before == 0) {
            setup.create("setup-test@example.test", password);
            assertThat(users.count()).isEqualTo(1);
            assertThat(users.findByEmailIgnoreCase("setup-test@example.test").orElseThrow().getEnabled()).isTrue();
        }
        assertThatThrownBy(() -> setup.create("another-test@example.test", password))
            .isInstanceOf(IllegalStateException.class);
        assertThat(users.count()).isEqualTo(Math.max(before, 1));
    }
}
