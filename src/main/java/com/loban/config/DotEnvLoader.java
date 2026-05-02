package com.loban.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Charge un fichier {@code .env} au démarrage (Maven / IDE ne le font pas nativement).
 * Les variables d'environnement réelles du système restent prioritaires.
 */
public final class DotEnvLoader {

    private DotEnvLoader() {
    }

    public static void load() {
        Path cwd = Path.of(System.getProperty("user.dir"));
        List<Path> bases = List.of(cwd, cwd.resolve("backend"));
        for (Path base : bases) {
            Path envFile = base.resolve(".env");
            if (!Files.isRegularFile(envFile)) {
                continue;
            }
            Dotenv dotenv = Dotenv.configure()
                    .directory(base.toAbsolutePath().normalize().toString())
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(e -> {
                String key = e.getKey();
                if (System.getenv(key) == null) {
                    System.setProperty(key, e.getValue());
                }
            });
            return;
        }
    }
}
