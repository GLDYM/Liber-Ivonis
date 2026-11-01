package com.liberivonis.config.layout;

import com.liberivonis.config.layout.HubLayout.ContainerNode;
import com.liberivonis.config.layout.HubLayout.ContainerType;
import com.liberivonis.config.layout.HubLayout.EntryNode;
import com.liberivonis.config.layout.HubLayout.Orientation;
import com.liberivonis.config.layout.HubLayout.TextNode;
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
        HubLayout layout = bundledLayout();
        ContainerNode root = assertInstanceOf(ContainerNode.class, layout.root());
        assertEquals(ContainerType.LINEAR, root.type());
        assertEquals(Orientation.VERTICAL, root.orientation());
        assertEquals(4, root.children().size());
        assertEquals(HubTheme.defaults(), layout.theme());
    }

    @Test
    void bundledLayoutKeepsMigratedCategoriesAndScreens() throws Exception {
        ContainerNode root = assertInstanceOf(ContainerNode.class, bundledLayout().root());
        ContainerNode scroll = assertInstanceOf(ContainerNode.class, root.children().get(2));
        assertEquals(4, scroll.children().size());

        String[] categoryKeys = {
                "screen.liber_ivonis.category.base",
                "screen.liber_ivonis.category.task",
                "screen.liber_ivonis.category.handbook",
                "screen.liber_ivonis.category.narcissus"
        };
        for (int index = 0; index < categoryKeys.length; index++) {
            ContainerNode section = assertInstanceOf(ContainerNode.class, scroll.children().get(index));
            TextNode heading = assertInstanceOf(TextNode.class, section.children().getFirst());
            assertEquals(categoryKeys[index], heading.text());
            assertInstanceOf(ContainerNode.class, section.children().get(1));
        }

        EntryNode keymap = entryAt(scroll, 0, 1);
        assertEquals("com.github.einjerjar.mc.keymap.client.gui.screen.KeymapScreen", keymap.target());
        assertEquals("<init>", keymap.method());
        assertEquals("entry.liber_ivonis.keymap", keymap.title());
        assertEquals("entry.liber_ivonis.keymap.description", keymap.description());

        EntryNode waypoints = entryAt(scroll, 3, 0);
        assertEquals("xin.vanilla.narcissus.integration.ScreenHelper", waypoints.target());
        assertEquals("openScreen", waypoints.method());
        assertEquals("entry.liber_ivonis.narcissus.waypoints", waypoints.title());
        assertEquals("entry.liber_ivonis.narcissus.waypoints.description", waypoints.description());

        EntryNode accessList = entryAt(scroll, 3, 1);
        assertEquals("openAccessListScreen", accessList.method());
        assertEquals("entry.liber_ivonis.narcissus.access_list", accessList.title());
        assertEquals("entry.liber_ivonis.narcissus.access_list.description", accessList.description());
    }

    private static HubLayout bundledLayout() throws Exception {
        try (InputStream input = HubLayoutLoaderTest.class.getResourceAsStream("/liber_ivonis/defaults/hub.json")) {
            if (input == null)
                throw new IOException("Bundled Hub layout is missing");
            return HubLayoutLoader.parse(
                    new InputStreamReader(input, StandardCharsets.UTF_8), "bundled default");
        }
    }

    private static EntryNode entryAt(ContainerNode scroll, int sectionIndex, int entryIndex) {
        ContainerNode section = assertInstanceOf(ContainerNode.class, scroll.children().get(sectionIndex));
        ContainerNode grid = assertInstanceOf(ContainerNode.class, section.children().get(1));
        return assertInstanceOf(EntryNode.class, grid.children().get(entryIndex));
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
