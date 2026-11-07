package com.tabaratodemais.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    @Profile("!bootstrap-admin")
    SecurityFilterChain security(HttpSecurity http, @org.springframework.beans.factory.annotation.Value("${site.indexing-enabled:false}") boolean indexing) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/produtos/*", "/rankings", "/rankings/*", "/robots.txt", "/sitemap.xml", "/sitemaps/*.xml").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.HEAD, "/produtos/*", "/rankings", "/rankings/*", "/robots.txt", "/sitemap.xml", "/sitemaps/*.xml").permitAll()
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/error", "/admin/login").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form.loginPage("/admin/login")
                .loginProcessingUrl("/admin/login").usernameParameter("email")
                .defaultSuccessUrl("/admin", true).failureUrl("/admin/login?error").permitAll())
            .logout(logout -> logout.logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout").invalidateHttpSession(true)
                .deleteCookies("JSESSIONID").permitAll())
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
            .headers(headers -> headers.addHeaderWriter((request,response) -> {
                if(!indexing || request.getRequestURI().startsWith("/admin") || !request.getParameterMap().isEmpty() || response.getStatus()>=400)
                    response.setHeader("X-Robots-Tag","noindex, nofollow");
            }).contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' https:; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")))
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

}
