package ro.scribemed.user.service;

import ro.scribemed.authentification.exception.EmailAlreadyRegisteredException;
import ro.scribemed.authentification.exception.InvalidCredentialsException;
import ro.scribemed.authentification.exception.UsernameAlreadyRegisteredException;
import ro.scribemed.authentification.security.PasswordHasher;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.user.domain.User;
import ro.scribemed.user.repository.JpaUserRepository;
import ro.scribemed.user.repository.UserRepository;

import java.util.Optional;

public final class UserService {

    private final JpaTransactionManager transactionManager;
    private final PasswordHasher passwordHasher;

    public UserService(
            JpaTransactionManager transactionManager, PasswordHasher passwordHasher
    ) {
        this.transactionManager = transactionManager;
        this.passwordHasher = passwordHasher;
    }

    public User register(
            String username,
            String email,
            String rawPassword

    ) {
        return transactionManager.execute(entityManager -> {
            UserRepository repository =
                    new JpaUserRepository(entityManager);

            if (repository.existsByUsername(username)) {
                throw new UsernameAlreadyRegisteredException(username);
            }

            if (repository.existsByEmail(email)) {
                throw new EmailAlreadyRegisteredException(email);
            }

            String hashPassword = passwordHasher.hash(rawPassword);

            User user = new User(username, email, hashPassword);

            repository.add(user);

            return user;
        });
    }

    public User authenticate(
            String email,
            String rawPassword
    ) {
        return transactionManager.execute(entityManager -> {
            UserRepository repository =
                    new JpaUserRepository(entityManager);


            Optional<User> user = repository.findByEmail(email);

            if(user.isEmpty()){
                throw new InvalidCredentialsException();
            }

            User currentUser = user.get();
            String hashPassword  = currentUser.getPasswordHash();

            if(!passwordHasher.matches(rawPassword, hashPassword)){
                throw new InvalidCredentialsException();
            }

            return currentUser;
        });
    }
}
