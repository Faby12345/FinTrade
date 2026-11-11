package ro.scribemed.authentification.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.Objects;

public class BCryptPasswordHasher implements PasswordHasher{

    private final int cost;

    public BCryptPasswordHasher(int cost) {
        if (cost < 4 || cost > 31) {
            throw new IllegalArgumentException(
                    "BCrypt cost must be between 4 and 31"
            );
        }

        this.cost = cost;
    }


    @Override
    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword);

        return BCrypt.withDefaults()
                .hashToString(
                        cost,
                        rawPassword.toCharArray()
                );
    }

    @Override
    public boolean matches(
            String rawPassword,
            String passwordHash)
    {
        Objects.requireNonNull(rawPassword);
        Objects.requireNonNull(passwordHash);

        return BCrypt.verifyer()
                .verify(
                        rawPassword.toCharArray(),
                        passwordHash
                )
                .verified;

    }
}
