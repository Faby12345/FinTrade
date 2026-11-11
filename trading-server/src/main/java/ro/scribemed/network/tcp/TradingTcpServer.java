package ro.scribemed.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TradingTcpServer implements AutoCloseable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TradingTcpServer.class);

    private final int port;
    private final ClientHandlerFactory clientHandlerFactory;
    private final ExecutorService executorService =
            Executors.newVirtualThreadPerTaskExecutor();

    private volatile ServerSocket serverSocket;

    public TradingTcpServer(
            int port,
            ClientHandlerFactory clientHandlerFactory
    ) {
        this.port = port;
        this.clientHandlerFactory = clientHandlerFactory;
    }

    public void start() throws IOException {
        if (serverSocket != null) {
            throw new IllegalStateException(
                    "Trading TCP server is already running"
            );
        }

        ServerSocket listeningSocket = new ServerSocket(port);
        serverSocket = listeningSocket;

        LOGGER.info(
                "FinTrade TCP server listening on port {}",
                port
        );

        try {
            while (!listeningSocket.isClosed()) {
                try {
                    Socket clientSocket =
                            listeningSocket.accept();

                    executorService.submit(
                            clientHandlerFactory.create(clientSocket)
                    );
                } catch (SocketException exception) {
                    if (!listeningSocket.isClosed()) {
                        throw exception;
                    }
                }
            }
        } finally {
            serverSocket = null;
        }
    }

    @Override
    public void close() throws IOException {
        ServerSocket activeSocket = serverSocket;

        if (activeSocket != null && !activeSocket.isClosed()) {
            activeSocket.close();
        }

        executorService.close();
    }

    public void closeQuietly() {
        try {
            close();
        } catch (IOException exception) {
            LOGGER.warn(
                    "Could not close the trading TCP server cleanly",
                    exception
            );
        }
    }
}
