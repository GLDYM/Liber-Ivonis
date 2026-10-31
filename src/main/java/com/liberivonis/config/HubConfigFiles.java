package com.liberivonis.config;

import com.liberivonis.LiberIvonis;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Creates and locates the editable JSON configuration used by the Hub. */
public final class HubConfigFiles {
    public static final String LAYOUT_FILE = "hub.json";
    public static final String DEFAULT_LAYOUT_RESOURCE = "/liber_ivonis/defaults/hub.json";
    public static final String README_RESOURCE = "/liber_ivonis/defaults/HUB_LAYOUT.md";
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/HubConfig");
    private static final List<String> DEFAULT_LANGUAGES = List.of("en_us", "zh_cn", "zh_hk", "zh_tw");

    public static Path root() {
        return FMLPaths.CONFIGDIR.get().resolve(LiberIvonis.MOD_ID);
    }

    public static Path layoutPath() {
        return root().resolve(LAYOUT_FILE);
    }

    public static Path langDirectory() {
        return root().resolve("lang");
    }

    public static Path languagePath(String language) {
        String normalized = language == null || language.isBlank()
                ? "en_us"
                : language.toLowerCase(Locale.ROOT).replace('-', '_').replaceAll("[^a-z0-9_]", "_");
        return langDirectory().resolve(normalized + ".json");
    }

    /**
     * Installs missing defaults without replacing any file already edited by a
     * user or modpack author.
     */
    public static void ensureDefaults() {
        try {
            Files.createDirectories(langDirectory());
            copyResourceIfMissing(DEFAULT_LAYOUT_RESOURCE, layoutPath());
            copyResourceIfMissing(README_RESOURCE, root().resolve("README.md"));
            for (String language : DEFAULT_LANGUAGES) {
                copyResourceIfMissing("/assets/" + LiberIvonis.MOD_ID + "/lang/" + language + ".json",
                        languagePath(language));
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Failed to create Liber Ivonis JSON config defaults in {}", root(), exception);
        }
    }

    private static void copyResourceIfMissing(String resource, Path target) throws IOException {
        if (Files.exists(target))
            return;
        try (InputStream input = HubConfigFiles.class.getResourceAsStream(resource)) {
            if (input == null) {
                LOGGER.error("Missing bundled config resource {}", resource);
                return;
            }
            Files.createDirectories(target.getParent());
            try {
                Files.copy(input, target);
                LOGGER.info("Created default Liber Ivonis config file {}", target);
            } catch (FileAlreadyExistsException ignored) {
                // Another setup callback created the same default first.
            }
        }
    }

    private HubConfigFiles() {
    }
}
