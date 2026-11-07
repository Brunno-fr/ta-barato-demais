package com.tabaratodemais.admin;

import com.tabaratodemais.user.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAuthenticationIT {
    @Autowired MockMvc mvc;
    @Autowired AdminUserRepository users;
    @Autowired PasswordEncoder encoder;
    private static final String PASSWORD = "Synthetic-test-only-123";

    private String account(boolean enabled) {
        String email = "test-" + UUID.randomUUID() + "@example.test";
        users.saveAndFlush(new AdminUser(email, encoder.encode(PASSWORD), enabled));
        return email;
    }

    @Test void authenticatesWithDatabaseRenewsSessionAndLogsOut() throws Exception {
        String email = account(true);
        var session = new MockHttpSession();
        String oldId = session.getId();
        session.setNew(false);
        var result = mvc.perform(post("/admin/login").session(session).with(csrf())
                .with(request -> {
                    request.setRequestedSessionId(oldId);
                    request.setRequestedSessionIdValid(true);
                    return request;
                })
                .param("email", " " + email.toUpperCase(java.util.Locale.ROOT) + " ").param("password", PASSWORD))
            .andExpect(redirectedUrl("/admin")).andExpect(authenticated().withUsername(email)).andReturn();
        var loggedIn = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(loggedIn.getId()).isNotEqualTo(oldId);
        mvc.perform(get("/admin").session(loggedIn)).andExpect(status().isOk());
        mvc.perform(post("/admin/logout").session(loggedIn).with(csrf()))
            .andExpect(redirectedUrl("/admin/login?logout")).andExpect(unauthenticated());
        assertThat(loggedIn.isInvalid()).isTrue();
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/admin/login"));
    }
    @Test void rejectsWrongPasswordUnknownAndDisabledAccountsWithoutDisclosure() throws Exception {
        String active = account(true);
        String disabled = account(false);
        String unknown = "missing-" + UUID.randomUUID() + "@example.test";
        for (String email : new String[]{active, disabled, unknown}) {
            mvc.perform(post("/admin/login").with(csrf()).param("email", email)
                    .param("password", email.equals(active) ? "wrong-password" : PASSWORD))
                .andExpect(redirectedUrl("/admin/login?error")).andExpect(unauthenticated());
        }
        mvc.perform(post("/admin/login").with(csrf()).param("email", active).param("password", "x".repeat(100)))
            .andExpect(redirectedUrl("/admin/login?error")).andExpect(unauthenticated());
    }
}
