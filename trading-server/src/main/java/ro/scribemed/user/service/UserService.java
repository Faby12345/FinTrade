package ro.scribemed.user.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.user.domain.User;
import ro.scribemed.user.repository.JpaUserRepository;
import ro.scribemed.user.repository.UserRepository;

import java.util.Optional;

public final class UserService {

    private final JpaTransactionManager transactionManager;

    public UserService(
            JpaTransactionManager transactionManager
    ) {
        this.transactionManager = transactionManager;
    }

    public User register(
            String username,
            String email
    ) {
        return transactionManager.execute(entityManager -> {
            UserRepository repository =
                    new JpaUserRepository(entityManager);

            if (repository.existsByUsername(username)) {
                throw new IllegalArgumentException(
                        "Username already exists"
                );
            }

            if (repository.existsByEmail(email)) {
                throw new IllegalArgumentException(
                        "Email already exists"
                );
            }

            User user = new User(username, email);

            repository.add(user);

            return user;
        });
    }

    public User login(
            String email
    ) {
        return transactionManager.execute(entityManager -> {
            UserRepository repository =
                    new JpaUserRepository(entityManager);


            Optional<User> user = repository.findByEmail(email);

            if(user.isEmpty()){
                throw new EntityNotFoundException("User dose not exist");
            }

            return user.get();
        });
    }
}