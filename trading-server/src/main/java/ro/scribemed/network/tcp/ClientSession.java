package ro.scribemed.network.tcp;

import java.util.Optional;
import java.util.UUID;

public final class ClientSession {

    private UUID authenticatedUserId;
    public void authenticate(UUID userId){
        authenticatedUserId = userId;
    }

    public boolean isAuthenticated(){
        return authenticatedUserId != null;
    }

    public Optional<UUID> authenticatedUserId() {
        return Optional.ofNullable(
                authenticatedUserId
        );
    }
}
