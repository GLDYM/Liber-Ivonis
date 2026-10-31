package com.liberivonis.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liberivonis.config.HubConfigFiles;
import com.liberivonis.config.LocalTranslationResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves Hub-configured labels from {@code config/liber_ivonis/lang} before
 * consulting Minecraft's loaded language resources.
 */
public final class HubLocalization {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/Localization");
    private final Map<String, String> selectedLanguage;
    private final Map<String, String> fallbackLanguage;

    private HubLocalization(Map<String, String> selectedLanguage, Map<String, String> fallbackLanguage) {
        this.selectedLanguage = selectedLanguage;
        this.fallbackLanguage = fallbackLanguage;
    }

    public static HubLocalization current() {
        HubConfigFiles.ensureDefaults();
        String selected = Minecraft.getInstance().getLanguageManager().getSelected();
        Map<String, String> primary = readLanguage(selected);
        Map<String, String> fallback = selected.equals("en_us") ? Map.of() : readLanguage("en_us");
        return new HubLocalization(primary, fallback);
    }

    public String resolve(String keyOrLiteral, String fallback) {
        return LocalTranslationResolver.resolve(keyOrLiteral, fallback, selectedLanguage, fallbackLanguage,
                I18n::exists, I18n::get);
    }

    public Component component(String keyOrLiteral, String fallback) {
        return Component.literal(resolve(keyOrLiteral, fallback));
    }

    private static Map<String, String> readLanguage(String language) {
        if (language == null || language.isBlank())
            return Map.of();
        String normalized = language.toLowerCase(Locale.ROOT).replace('-', '_');
        Path path = HubConfigFiles.langDirectory().resolve(normalized + ".json");
        if (!Files.isRegularFile(path))
            return Map.of();

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject())
                throw new IOException("root must be a JSON object");
            JsonObject object = parsed.getAsJsonObject();
            Map<String, String> translations = new LinkedHashMap<>(object.size());
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive())
                    throw new IOException("translation '" + entry.getKey() + "' must be a string");
                translations.put(entry.getKey(), value.getAsString());
            }
            return Map.copyOf(translations);
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("Could not read Liber Ivonis language override {}. Global translations will be used.", path,
                    exception);
            return Map.of();
        }
    }
}
