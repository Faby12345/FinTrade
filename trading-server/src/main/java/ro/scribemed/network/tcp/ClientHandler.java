package ro.scribemed.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ClientHandler implements Runnable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final RequestRouter requestRouter;

    public ClientHandler(
            Socket socket,
            RequestRouter requestRouter
    ) {
        this.socket = socket;
        this.requestRouter = requestRouter;
    }

    @Override
    public void run() {
        String remoteAddress = String.valueOf(
                socket.getRemoteSocketAddress()
        );

        LOGGER.info("Client connected: {}", remoteAddress);

        try (
                Socket clientSocket = socket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                clientSocket.getOutputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            ClientSession session = new ClientSession();
            String requestLine;

            while ((requestLine = reader.readLine()) != null) {
                if (requestLine.isBlank()) {
                    continue;
                }

                String response = requestRouter.handle(
                        requestLine,
                        session
                );

                writer.write(response);
                writer.newLine();
                writer.flush();
            }

            LOGGER.info("Client disconnected: {}", remoteAddress);
        } catch (IOException exception) {
            LOGGER.info(
                    "Client connection ended: {} ({})",
                    remoteAddress,
                    exception.getMessage()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Unexpected client handler failure: {}",
                    remoteAddress,
                    exception
            );
        }
    }
}
