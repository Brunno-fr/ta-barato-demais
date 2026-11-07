package com.tabaratodemais.admin;

import com.tabaratodemais.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean UserDetailsService users;

    @Test void loginIsPublicAndContainsCsrfToken() throws Exception {
        mvc.perform(get("/admin/login")).andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"_csrf\"")))
            .andExpect(content().string(containsString("autocomplete=\"current-password\"")));
    }
    @Test void adminCanSeeDashboardAndLogoutForm() throws Exception {
        mvc.perform(get("/admin").with(user("admin@example.test").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("admin@example.test")))
            .andExpect(content().string(containsString("action=\"/admin/logout\"")));
    }
    @Test void authenticatedNonAdminCannotEnter() throws Exception {
        mvc.perform(get("/admin").with(user("visitor").roles("USER"))).andExpect(status().isForbidden());
    }
    @Test void loginAndLogoutRequireCsrf() throws Exception {
        mvc.perform(post("/admin/login").param("email","test@example.test").param("password","invalid"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/admin/logout").with(user("admin").roles("ADMIN")))
            .andExpect(status().isForbidden());
    }
}
