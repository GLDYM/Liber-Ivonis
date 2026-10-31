package com.liberivonis.config.layout;

import com.liberivonis.config.layout.HubLayout.ContainerNode;
import com.liberivonis.config.layout.HubLayout.ContainerType;
import com.liberivonis.config.layout.HubLayout.EntryNode;
import com.liberivonis.config.layout.HubLayout.Orientation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubLayoutLoaderTest {
    @Test
    void bundledLayoutParses() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/liber_ivonis/defaults/hub.json")) {
            if (input == null)
                throw new IOException("Bundled Hub layout is missing");
            HubLayout layout = HubLayoutLoader.parse(
                    new InputStreamReader(input, StandardCharsets.UTF_8), "bundled default");
            ContainerNode root = assertInstanceOf(ContainerNode.class, layout.root());
            assertEquals(ContainerType.LINEAR, root.type());
            assertEquals(Orientation.VERTICAL, root.orientation());
            assertEquals(4, root.children().size());
            assertEquals(HubTheme.defaults(), layout.theme());
        }
    }

    @Test
    void nestedLayoutAndEntryAreParsed() throws Exception {
        HubLayout layout = parse("""
                {
                  "schemaVersion": 1,
                  "layout": {
                    "type": "row",
                    "gap": 6,
                    "children": [
                      {
                        "type": "entry",
                        "item": "ageratum:guidebook",
                        "title": "pack.guide",
                        "fallback": "Guide",
                        "weight": 1
                      },
                      {"type": "spacer", "width": "12dp"}
                    ]
                  }
                }
                """);

        ContainerNode root = assertInstanceOf(ContainerNode.class, layout.root());
        assertEquals(Orientation.HORIZONTAL, root.orientation());
        assertEquals(6, root.gap());
        EntryNode entry = assertInstanceOf(EntryNode.class, root.children().getFirst());
        assertEquals("item", entry.kind());
        assertEquals("ageratum:guidebook", entry.target());
        assertEquals("pack.guide", entry.title());
        assertEquals(1, entry.style().weight());
        assertEquals(48, entry.style().minHeight());
        assertEquals(HubTheme.defaults(), layout.theme());
    }

    @Test
    void themeOverridesAndDefaultsAreParsed() throws Exception {
        HubLayout layout = parse("""
                {
                  "schemaVersion": 1,
                  "theme": {
                    "background": "#11223344",
                    "accent": "#ABCDEF"
                  },
                  "layout": {"type": "column", "children": []}
                }
                """);
        assertEquals(0x11223344, layout.theme().background());
        assertEquals(0xFFABCDEF, layout.theme().accent());
        assertEquals(HubTheme.defaults().surface(), layout.theme().surface());
    }

    @Test
    void invalidThemeColorIsRejected() {
        IOException error = assertThrows(IOException.class, () -> parse("""
                {
                  "schemaVersion": 1,
                  "theme": {"background": "transparent"},
                  "layout": {"type": "column", "children": []}
                }
                """));
        assertTrue(error.getMessage().contains("theme.background"));
    }

    @Test
    void invalidNodeColorIsRejected() {
        IOException error = assertThrows(IOException.class, () -> parse("""
                {
                  "schemaVersion": 1,
                  "layout": {
                    "type": "text",
                    "text": "hub.title",
                    "color": "not-a-colour"
                  }
                }
                """));
        assertTrue(error.getMessage().contains("layout.color"));
    }

    @Test
    void scrollRequiresAChild() {
        IOException error = assertThrows(IOException.class, () -> parse("""
                {
                  "schemaVersion": 1,
                  "layout": {"type": "scroll", "children": []}
                }
                """));
        assertTrue(error.getMessage().contains("at least one child"));
    }

    @Test
    void entryRequiresExactlyOneTarget() {
        IOException error = assertThrows(IOException.class, () -> parse("""
                {
                  "schemaVersion": 1,
                  "layout": {
                    "type": "column",
                    "children": [
                      {"type": "entry", "builtin": "patchouli", "item": "patchouli:guide_book"}
                    ]
                  }
                }
                """));
        assertTrue(error.getMessage().contains("exactly one"));
    }

    @Test
    void unsupportedSchemaIsRejected() {
        IOException error = assertThrows(IOException.class, () -> parse("""
                {
                  "schemaVersion": 99,
                  "layout": {"type": "column", "children": []}
                }
                """));
        assertTrue(error.getMessage().contains("schemaVersion"));
    }

    private static HubLayout parse(String json) throws IOException {
        try (Reader reader = new StringReader(json)) {
            return HubLayoutLoader.parse(reader, "test");
        }
    }
}
