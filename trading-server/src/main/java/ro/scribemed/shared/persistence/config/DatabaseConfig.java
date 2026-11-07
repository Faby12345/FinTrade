package ro.scribemed.shared.persistence.config;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password
) {

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                environmentOrDefault(
                        "FINTRADE_DB_HOST",
                        "localhost"
                ),
                Integer.parseInt(
                        environmentOrDefault(
                                "FINTRADE_DB_PORT",
                                "3306"
                        )
                ),
                environmentOrDefault(
                        "FINTRADE_DB_NAME",
                        "fintrade"
                ),
                requiredEnvironmentVariable(
                        "FINTRADE_DB_USER"
                ),
                requiredEnvironmentVariable(
                        "FINTRADE_DB_PASSWORD"
                )
        );
    }

    public String jdbcUrl() {
        return "jdbc:mysql://%s:%d/%s"
                .formatted(host, port, database);
    }

    private static String environmentOrDefault(
            String name,
            String defaultValue
    ) {
        String value = System.getenv(name);

        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    private static String requiredEnvironmentVariable(
            String name
    ) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
            );
        }

        return value;
    }
}