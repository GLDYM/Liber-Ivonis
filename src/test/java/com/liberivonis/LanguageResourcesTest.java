package com.liberivonis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LanguageResourcesTest {
    @Test
    void bundledLanguagesContainTheSameKeys() {
        Set<String> english = keys("en_us");
        for (String language : new String[] { "zh_cn", "zh_hk", "zh_tw" })
            assertEquals(english, keys(language), language + " translation keys differ from en_us");
    }

    private static Set<String> keys(String language) {
        String path = "/assets/liber_ivonis/lang/" + language + ".json";
        try (InputStream input = LanguageResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing language resource " + path);
            JsonObject object = JsonParser.parseReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            return object.keySet();
        } catch (Exception exception) {
            throw new AssertionError("Could not read language resource " + path, exception);
        }
    }
}
