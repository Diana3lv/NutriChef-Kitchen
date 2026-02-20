package org.dsoft.entity;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {
    
    @Column(unique = true, nullable = false)
    public String email;
    
    @Column(nullable = false)
    public String password;

    @Column(nullable = false)
    public String firstName;
    
    @Column(nullable = false)
    public String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserRole role;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column(nullable = false)
    public LocalDateTime updatedAt;

    @Column(nullable = false)
    public boolean isActive;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static boolean existsByEmail(String email) {
        return find("email", email).count() > 0;
    }
}