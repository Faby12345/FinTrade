package ro.scribemed.user.repository;

import ro.scribemed.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void add(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
