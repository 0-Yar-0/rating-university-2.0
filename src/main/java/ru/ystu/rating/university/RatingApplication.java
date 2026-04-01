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
		String jdbcEnv = getenvTrimmed("JDBC_DATABASE_URL");
		String jdbcProp = System.getProperty("spring.datasource.url");
		if ((jdbcEnv != null && !jdbcEnv.isBlank()) || (jdbcProp != null && !jdbcProp.isBlank())) {
			return;
		}

		String databaseUrl = getenvTrimmed("DATABASE_URL");
		if (databaseUrl == null || databaseUrl.isBlank()) {
			return;
		}

		try {
			URI uri = new URI(databaseUrl);
			String scheme = uri.getScheme();
			if (scheme == null || !("postgres".equalsIgnoreCase(scheme) || "postgresql".equalsIgnoreCase(scheme))) {
				return;
			}

			String host = uri.getHost();
			if (host == null || host.isBlank()) {
				return;
			}

			int port = uri.getPort() > 0 ? uri.getPort() : 5432;
			String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
			String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
			if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
				jdbcUrl += "?" + uri.getQuery();
			}
			System.setProperty("spring.datasource.url", jdbcUrl);

			if (System.getProperty("spring.datasource.username") == null
					&& getenvTrimmed("JDBC_DATABASE_USERNAME") == null
					&& getenvTrimmed("PGUSER") == null) {
				String userInfo = uri.getUserInfo();
				if (userInfo != null && !userInfo.isBlank()) {
					String[] parts = userInfo.split(":", 2);
					if (parts.length > 0 && !parts[0].isBlank()) {
						System.setProperty("spring.datasource.username", parts[0]);
					}
					if (parts.length > 1 && !parts[1].isBlank()
							&& System.getProperty("spring.datasource.password") == null
							&& getenvTrimmed("JDBC_DATABASE_PASSWORD") == null
							&& getenvTrimmed("PGPASSWORD") == null) {
						System.setProperty("spring.datasource.password", parts[1]);
					}
				}
			}
		} catch (URISyntaxException ignored) {
			// Keep startup resilient if DATABASE_URL is malformed.
		}
	}

	private static String getenvTrimmed(String key) {
		String value = System.getenv(key);
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}
