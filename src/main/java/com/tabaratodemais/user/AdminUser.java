package com.tabaratodemais.user;
import jakarta.persistence.*;
import com.tabaratodemais.shared.BaseEntity;
@Entity
@Table(name = "users")

public class AdminUser extends BaseEntity {
    @Column(nullable = false, length = 254)
    private String email;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 20)
    private String role = "ADMIN";
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false, updatable = false)
    private java.time.Instant createdAt = java.time.Instant.now();
    protected AdminUser() {}

    public AdminUser(String email, String passwordHash, boolean enabled) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public String getPasswordHash() { return passwordHash; }

    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean getEnabled() { return enabled; }
    public java.time.Instant getCreatedAt() { return createdAt; }
}
