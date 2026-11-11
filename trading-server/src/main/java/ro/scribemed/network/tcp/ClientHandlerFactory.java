package ro.scribemed.network.tcp;

import java.net.Socket;

@FunctionalInterface
public interface ClientHandlerFactory {
    Runnable create(Socket socket);
}
