package ro.scribemed;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.authentification.security.BCryptPasswordHasher;
import ro.scribemed.authentification.security.PasswordHasher;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.shared.persistence.PersistenceManager;
import ro.scribemed.shared.persistence.config.DatabaseConfig;
import ro.scribemed.user.domain.User;
import ro.scribemed.user.repository.JpaUserRepository;
import ro.scribemed.user.repository.UserRepository;
import ro.scribemed.user.service.UserService;

import java.util.Locale;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Logger logger =
            LoggerFactory.getLogger(Main.class);

    private static void testUserCreation(DatabaseConfig databaseConfig){
        try (PersistenceManager persistenceManager =
                     new PersistenceManager("fintrade", databaseConfig)) {


            JpaTransactionManager transactionManager =
                    new JpaTransactionManager(
                            persistenceManager.entityManagerFactory()
                    );


            logger.info(
                    "CONNECTED TO DATABASE {} at {}:{}",
                    databaseConfig.database(),
                    databaseConfig.host(),
                    databaseConfig.port()
            );


            PasswordHasher passwordHasher = new BCryptPasswordHasher(4);
            UserService userService = new UserService(
                    transactionManager,
                    passwordHasher
            );

            logger.info("CREATING A NEW USER...");

            User registerdUser = userService.register(
                    "Andrei Balan",
                    "andrei2@gmail,com",
                    "fabian123"
            );

            logger.info("USER CREATED - NAME: {}, EMAIL: {}, PASSWORD: {}",
                    registerdUser.getUsername(),
                    registerdUser.getEmail(),
                    registerdUser.getPasswordHash());

            logger.info("TRYING TO LOG IN...");

            User loggedInUser = userService
                    .login(
                            "andrei2@gmail,com",
                            "fabian123"
                    );

            logger.info("USER LOGGED IN");




            // Start server and keep this block active
        } catch (PersistenceException exception) {

            logger.error(
                    "Could not initialize persistence: {}",
                    exception.getMessage()
            );

        }

    }

    public static void main(String[] args) {
        logger.info("CONNECTING TO DATABASE...");

        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();
        testUserCreation(databaseConfig);

    }
}