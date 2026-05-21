package client.gui;

import java.nio.file.Files;
import java.nio.file.Path;

public final class DeployPathResolver {

    private DeployPathResolver() {
    }

    /**
     * Directory that should contain a {@code Corpus} subdirectory (or be used as default workspace root).
     */
    public static Path resolveDeployRoot() {
        String prop = System.getProperty("lab5.deploy.root");
        if (prop != null && !prop.isBlank()) {
            Path p = Path.of(prop).toAbsolutePath().normalize();
            if (Files.isDirectory(p)) {
                return p;
            }
        }
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(cwd.resolve("Corpus"))) {
            return cwd;
        }
        Path candidate = cwd.resolve("../..").normalize();
        if (Files.isDirectory(candidate.resolve("Corpus"))) {
            return candidate;
        }
        return cwd;
    }
}
