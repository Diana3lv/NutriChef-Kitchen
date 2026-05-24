package org.dsoft.entity.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        try {
            return UserRole.valueOf(dbValue);
        } catch (IllegalArgumentException e) {
            // unknown role - treat as a regular user
            return UserRole.USER;
        }
    }
}
