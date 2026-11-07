package ro.scribemed;

import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.shared.persistence.PersistenceManager;
import ro.scribemed.shared.persistence.config.DatabaseConfig;

import java.util.Locale;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Logger logger =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("CONNECTING TO DATABASE...");

        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();


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

            // Start server and keep this block active
        } catch (PersistenceException exception) {

            logger.error(
                    "Could not initialize persistence: {}",
                    exception.getMessage()
            );

        }
    }
}