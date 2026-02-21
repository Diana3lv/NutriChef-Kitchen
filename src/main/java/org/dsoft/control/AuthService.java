package org.dsoft.control;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.dsoft.entity.dto.AuthResponse;
import org.dsoft.entity.dto.LoginRequest;
import org.dsoft.entity.dto.RegisterRequest;
import org.dsoft.entity.dto.UserDTO;
import org.dsoft.entity.model.User;
import org.dsoft.entity.model.UserRole;
import org.dsoft.exception.AuthenticationException;
import org.dsoft.exception.ValidationException;
import org.mindrot.jbcrypt.BCrypt;

import io.smallrye.jwt.build.Jwt;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;


@ApplicationScoped
public class AuthService {

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (User.existsByEmail(request.email())) {
            throw new ValidationException("Email already in use");
        }
        
        validatePassword(request.password());
        
        User user = new User();
        user.email = request.email();
        user.firstName = request.firstName();
        user.lastName = request.lastName();
        user.password = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        user.role = UserRole.USER;
        user.isActive = true;
        
        user.persist();
        
        String token = generateToken(user);
        UserDTO userDTO = user.toUserDTO();
        return new AuthResponse(token, userDTO);
    }

    public AuthResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email());
        
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (!BCrypt.checkpw(request.password(), user.password)) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = generateToken(user);
        UserDTO userDTO = user.toUserDTO();
        return new AuthResponse(token, userDTO);
    }

    private String generateToken(User user) {
        return Jwt.issuer("https://nutrichef.io")
                .subject(user.id.toString())
                .claim("email", user.email)
                .claim("role", user.role.name())
                .groups(new HashSet<>(Set.of(user.role.name())))
                .expiresIn(Duration.ofDays(30))
                .sign();
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("Password must contain at least one number");
        }
        if (!password.matches(".*[@$!%*?&].*")) {
            throw new ValidationException("Password must contain at least one special character");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }
    }
}