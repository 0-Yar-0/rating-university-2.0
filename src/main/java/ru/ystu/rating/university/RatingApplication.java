package ru.ystu.rating.university;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RatingApplication {

	public static void main(String[] args) {
		applyRenderDatabaseDefaults();
		SpringApplication.run(RatingApplication.class, args);
	}

	private static void applyRenderDatabaseDefaults() {
		String rawUrl = firstNonBlank(
				System.getProperty("spring.datasource.url"),
				getenvTrimmed("JDBC_DATABASE_URL"),
				getenvTrimmed("SPRING_DATASOURCE_URL"),
				getenvTrimmed("DATABASE_URL"));
		if (rawUrl == null) {
			return;
		}

		ParsedDatabaseUrl parsed = parseDatabaseUrl(rawUrl);
		if (parsed == null) {
			return;
		}

		System.setProperty("spring.datasource.url", parsed.jdbcUrl());

		if (System.getProperty("spring.datasource.username") == null
				&& getenvTrimmed("JDBC_DATABASE_USERNAME") == null
				&& getenvTrimmed("SPRING_DATASOURCE_USERNAME") == null
				&& getenvTrimmed("PGUSER") == null
				&& parsed.username() != null
				&& !parsed.username().isBlank()) {
			System.setProperty("spring.datasource.username", parsed.username());
		}

		if (System.getProperty("spring.datasource.password") == null
				&& getenvTrimmed("JDBC_DATABASE_PASSWORD") == null
				&& getenvTrimmed("SPRING_DATASOURCE_PASSWORD") == null
				&& getenvTrimmed("PGPASSWORD") == null
				&& parsed.password() != null
				&& !parsed.password().isBlank()) {
			System.setProperty("spring.datasource.password", parsed.password());
		}
	}

	private static ParsedDatabaseUrl parseDatabaseUrl(String rawUrl) {
		String trimmed = rawUrl == null ? null : rawUrl.trim();
		if (trimmed == null || trimmed.isBlank()) {
			return null;
		}

		try {
			if (trimmed.startsWith("jdbc:postgresql://")) {
				return parsePostgresUri(new URI(trimmed.substring("jdbc:".length())));
			}
			if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
				return parsePostgresUri(new URI(trimmed));
			}
			return null;
		} catch (URISyntaxException ignored) {
			// Keep startup resilient if an env URL is malformed.
			return null;
		}
	}

	private static ParsedDatabaseUrl parsePostgresUri(URI uri) {
		String scheme = uri.getScheme();
		if (scheme == null || !("postgres".equalsIgnoreCase(scheme) || "postgresql".equalsIgnoreCase(scheme))) {
			return null;
		}

		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			return null;
		}

		int port = uri.getPort() > 0 ? uri.getPort() : 5432;
		String database = uri.getPath() == null ? "postgres" : uri.getPath().replaceFirst("^/", "");
		if (database.isBlank()) {
			database = "postgres";
		}

		String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
		if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
			jdbcUrl += "?" + uri.getQuery();
		}

		String username = null;
		String password = null;
		String userInfo = uri.getUserInfo();
		if (userInfo != null && !userInfo.isBlank()) {
			String[] parts = userInfo.split(":", 2);
			if (parts.length > 0 && !parts[0].isBlank()) {
				username = parts[0];
			}
			if (parts.length > 1 && !parts[1].isBlank()) {
				password = parts[1];
			}
		}

		return new ParsedDatabaseUrl(jdbcUrl, username, password);
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static String getenvTrimmed(String key) {
		String value = System.getenv(key);
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private record ParsedDatabaseUrl(String jdbcUrl, String username, String password) {
	}

}
