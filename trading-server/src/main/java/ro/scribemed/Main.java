package ro.scribemed;

import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.authentification.handler.AuthenticationHandler;
import ro.scribemed.authentification.security.BCryptPasswordHasher;
import ro.scribemed.authentification.security.PasswordHasher;
import ro.scribemed.network.tcp.ClientHandler;
import ro.scribemed.network.tcp.RequestRouter;
import ro.scribemed.network.tcp.TradingTcpServer;
import ro.scribemed.shared.persistence.JpaTransactionManager;
import ro.scribemed.shared.persistence.PersistenceManager;
import ro.scribemed.shared.persistence.config.DatabaseConfig;
import ro.scribemed.user.service.UserService;

import java.io.IOException;

public final class Main {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Main.class);

    private static final int DEFAULT_TCP_PORT = 9090;
    private static final int BCRYPT_COST = 12;

    private Main() {
    }

    public static void main(String[] args) {
        try {
            DatabaseConfig databaseConfig =
                    DatabaseConfig.fromEnvironment();

            startServer(databaseConfig);
        } catch (IllegalStateException exception) {
            LOGGER.error(
                    "Invalid server configuration: {}",
                    exception.getMessage()
            );
        } catch (PersistenceException exception) {
            LOGGER.error(
                    "Could not initialize persistence",
                    exception
            );
        } catch (IOException exception) {
            LOGGER.error(
                    "Trading server stopped because of an I/O error",
                    exception
            );
        }
    }

    private static void startServer(
            DatabaseConfig databaseConfig
    ) throws IOException {
        LOGGER.info(
                "Connecting to database {} at {}:{}",
                databaseConfig.database(),
                databaseConfig.host(),
                databaseConfig.port()
        );

        try (PersistenceManager persistenceManager =
                     new PersistenceManager(
                             "fintrade",
                             databaseConfig
                     )) {
            JpaTransactionManager transactionManager =
                    new JpaTransactionManager(
                            persistenceManager.entityManagerFactory()
                    );

            PasswordHasher passwordHasher =
                    new BCryptPasswordHasher(BCRYPT_COST);

            UserService userService = new UserService(
                    transactionManager,
                    passwordHasher
            );

            AuthenticationHandler authenticationHandler =
                    new AuthenticationHandler(userService);

            RequestRouter requestRouter =
                    new RequestRouter(authenticationHandler);

            int tcpPort = tcpPortFromEnvironment();

            try (TradingTcpServer server =
                         new TradingTcpServer(
                                 tcpPort,
                                 socket -> new ClientHandler(
                                         socket,
                                         requestRouter
                                 )
                         )) {
                Thread shutdownHook = new Thread(
                        server::closeQuietly,
                        "fintrade-shutdown"
                );

                Runtime.getRuntime().addShutdownHook(shutdownHook);

                LOGGER.info(
                        "FinTrade TCP server starting on port {}",
                        tcpPort
                );

                try {
                    server.start();
                } finally {
                    removeShutdownHook(shutdownHook);
                }
            }
        }
    }

    private static int tcpPortFromEnvironment() {
        String configuredPort = System.getenv(
                "FINTRADE_TCP_PORT"
        );

        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_TCP_PORT;
        }

        try {
            int port = Integer.parseInt(configuredPort);

            if (port < 1 || port > 65_535) {
                throw new IllegalStateException(
                        "FINTRADE_TCP_PORT must be between 1 and 65535"
                );
            }

            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "FINTRADE_TCP_PORT must be a number",
                    exception
            );
        }
    }

    private static void removeShutdownHook(Thread shutdownHook) {
        try {
            Runtime.getRuntime().removeShutdownHook(
                    shutdownHook
            );
        } catch (IllegalStateException ignored) {
            // The JVM is already shutting down.
        }
    }
}
