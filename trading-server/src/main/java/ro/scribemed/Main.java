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
import ro.scribemed.stock.domain.Stock;
import ro.scribemed.stock.handler.StockHandler;
import ro.scribemed.stock.repository.JpaStockRepository;
import ro.scribemed.stock.repository.StockRepository;
import ro.scribemed.stock.service.StockService;
import ro.scribemed.tradingAccount.handler.TradingAccountHandler;
import ro.scribemed.tradingAccount.service.TradingAccountService;
import ro.scribemed.user.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

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

            seedMockStocksIfEmpty(transactionManager);

            PasswordHasher passwordHasher =
                    new BCryptPasswordHasher(BCRYPT_COST);


            /// INIT USERS / AUTH ENTRY POINT
            UserService userService = new UserService(
                    transactionManager,
                    passwordHasher
            );

            AuthenticationHandler authenticationHandler =
                    new AuthenticationHandler(userService);


            /// INIT STOCKS ENTRY POINT
            StockService stockService =
                    new StockService(transactionManager);

            StockHandler stockHandler = new
                    StockHandler(stockService);

            /// INIT TRADING ACCOUNT ENTRY POINT

            TradingAccountService tradingAccountService =
                    new TradingAccountService(transactionManager);

            TradingAccountHandler tradingAccountHandler =
                    new TradingAccountHandler(tradingAccountService);


            RequestRouter requestRouter =
                    new RequestRouter(authenticationHandler, stockHandler, tradingAccountHandler);

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

    private static void seedMockStocksIfEmpty(
            JpaTransactionManager transactionManager
    ) {
        transactionManager.execute(entityManager -> {
            StockRepository stockRepository =
                    new JpaStockRepository(entityManager);

            if (stockRepository.count() > 0) {
                return;
            }

            List<Stock> mockStocks = List.of(
                    new Stock("AAPL", "Apple Inc.", new BigDecimal("275.40")),
                    new Stock("MSFT", "Microsoft Corp.", new BigDecimal("485.25")),
                    new Stock("GOOGL", "Alphabet Inc.", new BigDecimal("312.80")),
                    new Stock("AMZN", "Amazon.com Inc.", new BigDecimal("245.15")),
                    new Stock("NVDA", "NVIDIA Corp.", new BigDecimal("198.70")),
                    new Stock("META", "Meta Platforms Inc.", new BigDecimal("710.30")),
                    new Stock("TSLA", "Tesla Inc.", new BigDecimal("462.90")),
                    new Stock("JPM", "JPMorgan Chase & Co.", new BigDecimal("305.60")),
                    new Stock("V", "Visa Inc.", new BigDecimal("372.45")),
                    new Stock("NFLX", "Netflix Inc.", new BigDecimal("1215.50"))
            );

            mockStocks.forEach(stockRepository::add);

            LOGGER.info(
                    "Initialized stock catalog with {} mock stocks",
                    mockStocks.size()
            );
        });
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
