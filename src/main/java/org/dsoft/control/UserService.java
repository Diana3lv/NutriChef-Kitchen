package org.dsoft.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.dsoft.entity.model.User;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    public User getUserById(Long userId) {
        return (User) User.findByIdOptional(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(User.findByEmail(email));
    }

    public boolean userExistsByEmail(String email) {
        return User.existsByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        user.persist();
        return user;
    }

    @Transactional
    public void updateUser(User user) {
        user.persist();
    }
}
