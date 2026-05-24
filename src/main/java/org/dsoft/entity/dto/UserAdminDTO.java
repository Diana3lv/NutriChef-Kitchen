package org.dsoft.entity.dto;

import java.time.LocalDateTime;

public class UserAdminDTO {
    public Long id;
    public String email;
    public String firstName;
    public String lastName;
    public String role;
    public LocalDateTime createdAt;
    public LocalDateTime lastLogin;
    public boolean isActive;
    public Long recipesCreatedCount;

    public UserAdminDTO() {}

    public UserAdminDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        LocalDateTime createdAt,
        LocalDateTime lastLogin,
        boolean isActive,
        Long recipesCreatedCount
    ) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.recipesCreatedCount = recipesCreatedCount;
    }
}
