package com.liberivonis.config.layout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.liberivonis.config.HubConfigFiles;
import com.liberivonis.config.layout.HubLayout.ButtonAction;
import com.liberivonis.config.layout.HubLayout.ButtonNode;
import com.liberivonis.config.layout.HubLayout.ContainerNode;
import com.liberivonis.config.layout.HubLayout.ContainerType;
import com.liberivonis.config.layout.HubLayout.Dimension;
import com.liberivonis.config.layout.HubLayout.EntryNode;
import com.liberivonis.config.layout.HubLayout.Insets;
import com.liberivonis.config.layout.HubLayout.Node;
import com.liberivonis.config.layout.HubLayout.Orientation;
import com.liberivonis.config.layout.HubLayout.SpacerNode;
import com.liberivonis.config.layout.HubLayout.Style;
import com.liberivonis.config.layout.HubLayout.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Loads and validates the editable JSON Hub layout. */
public final class HubLayoutLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/HubLayout");
    private static final int MAX_DEPTH = 16;
    private static final int MAX_NODES = 1000;

    public static HubLayout load() {
        return loadResult().layout();
    }

    /** Loads the layout and exposes whether the bundled fallback is active. */
    public static LoadResult loadResult() {
        HubConfigFiles.ensureDefaults();
        Path path = HubConfigFiles.layoutPath();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                return new LoadResult(parse(reader, path.toString()), false, "");
            } catch (IOException | RuntimeException exception) {
                LOGGER.error("Could not load Hub layout {}. The bundled layout will be used.", path, exception);
                String message = exception.getMessage();
                return new LoadResult(loadBundled(), true, message == null ? exception.toString() : message);
            }
        } else {
            LOGGER.warn("Hub layout {} does not exist. The bundled layout will be used.", path);
            return new LoadResult(loadBundled(), true, "Missing file: " + path);
        }
    }

    public static HubLayout parse(Reader reader, String source) throws IOException {
        final JsonElement parsed;
        try {
            parsed = JsonParser.parseReader(reader);
        } catch (JsonParseException exception) {
            throw new IOException(source + ": invalid JSON: " + exception.getMessage(), exception);
        }
        if (!parsed.isJsonObject())
            throw error(source, "root", "expected a JSON object");

        JsonObject document = parsed.getAsJsonObject();
        int schemaVersion = intValue(document.get("schemaVersion"), source, "schemaVersion", 0);
        if (schemaVersion != HubLayout.CURRENT_SCHEMA_VERSION) {
            throw error(source, "schemaVersion", "expected " + HubLayout.CURRENT_SCHEMA_VERSION
                    + ", found " + schemaVersion);
        }
        JsonObject layout = object(document.get("layout"), source, "layout");
        HubTheme theme = parseTheme(document.get("theme"), source);
        ParseContext context = new ParseContext(source);
        Node root = parseNode(layout, "layout", context, 0, true);
        return new HubLayout(schemaVersion, theme, root, source);
    }

    private static HubLayout loadBundled() {
        try (InputStream input = HubLayoutLoader.class.getResourceAsStream(HubConfigFiles.DEFAULT_LAYOUT_RESOURCE)) {
            if (input == null)
                throw new IOException("Missing " + HubConfigFiles.DEFAULT_LAYOUT_RESOURCE);
            return parse(new InputStreamReader(input, StandardCharsets.UTF_8), "bundled default");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the bundled Liber Ivonis Hub layout", exception);
        }
    }

    private static Node parseNode(JsonObject object, String path, ParseContext context, int depth,
            boolean root) throws IOException {
        if (depth > MAX_DEPTH)
            throw error(context.source, path, "maximum nesting depth of " + MAX_DEPTH + " exceeded");
        if (++context.nodes > MAX_NODES)
            throw error(context.source, path, "maximum node count of " + MAX_NODES + " exceeded");

        String rawType = requiredString(object, "type", context.source, path);
        String type = rawType.toLowerCase(Locale.ROOT);
        return switch (type) {
            case "column" -> parseContainer(object, path, context, depth, Orientation.VERTICAL, ContainerType.LINEAR);
            case "row" -> parseContainer(object, path, context, depth, Orientation.HORIZONTAL, ContainerType.LINEAR);
            case "linear" -> {
                Orientation orientation = parseOrientation(optionalString(object, "orientation", context.source,
                        path, "vertical"),
                        context, path);
                yield parseContainer(object, path, context, depth, orientation, ContainerType.LINEAR);
            }
            case "scroll" -> parseContainer(object, path, context, depth, Orientation.VERTICAL, ContainerType.SCROLL);
            case "grid" -> parseContainer(object, path, context, depth, Orientation.VERTICAL, ContainerType.GRID);
            case "text" -> parseText(object, path, context, root);
            case "entry" -> parseEntry(object, path, context);
            case "button" -> parseButton(object, path, context);
            case "spacer" -> new SpacerNode(parseStyle(object, path, context,
                    Dimension.wrapContent(), Dimension.wrapContent(), Insets.NONE, null, null));
            default -> throw error(context.source, path, "unknown node type '" + rawType
                    + "' (expected column, row, linear, scroll, grid, text, entry, button or spacer)");
        };
    }

    private static ContainerNode parseContainer(JsonObject object, String path, ParseContext context, int depth,
            Orientation orientation, ContainerType type) throws IOException {
        JsonArray childArray = array(object.get("children"), context.source, path + ".children");
        List<Node> children = new ArrayList<>(childArray.size());
        for (int i = 0; i < childArray.size(); i++) {
            String childPath = path + ".children[" + i + "]";
            if (!childArray.get(i).isJsonObject())
                throw error(context.source, childPath, "expected a JSON object");
            children.add(parseNode(childArray.get(i).getAsJsonObject(), childPath, context, depth + 1, false));
        }
        if (type == ContainerType.SCROLL && children.isEmpty())
            throw error(context.source, path, "a scroll node needs at least one child");

        int columns = type == ContainerType.GRID
                ? boundedInt(object.get("columns"), context.source, path + ".columns", 2, 1, 12)
                : 1;
        int gap = boundedInt(object.get("gap"), context.source, path + ".gap", 0, 0, 128);
        boolean fillViewport = optionalBoolean(object, "fillViewport", context.source, path,
                type == ContainerType.SCROLL);
        boolean smoothScrolling = optionalBoolean(object, "smoothScrolling", context.source, path,
                type == ContainerType.SCROLL);
        Style style = parseStyle(object, path, context, Dimension.matchParent(), Dimension.wrapContent(),
                Insets.NONE, null, null);
        return new ContainerNode(type, orientation, columns, gap, fillViewport, smoothScrolling, children, style);
    }

    private static TextNode parseText(JsonObject object, String path, ParseContext context, boolean root)
            throws IOException {
        String text = requiredString(object, "text", context.source, path);
        String fallback = optionalString(object, "fallback", context.source, path, "");
        float size = boundedFloat(object.get("size"), context.source, path + ".size", root ? 22 : 14, 1, 128);
        String color = optionalString(object, "color", context.source, path, "accent");
        String gravity = optionalString(object, "textGravity", context.source, path,
                optionalString(object, "textAlign", context.source, path, "left"));
        validateColor(color, context.source, path + ".color", false);
        validateGravity(gravity, context.source, path + ".textGravity");
        int maxLines = boundedInt(object.get("maxLines"), context.source, path + ".maxLines", 0, 0, 100);
        Dimension defaultWidth = root ? Dimension.matchParent() : Dimension.wrapContent();
        Style style = parseStyle(object, path, context, defaultWidth, Dimension.wrapContent(),
                Insets.NONE, null, null);
        return new TextNode(text, fallback, size, color, gravity, maxLines, style);
    }

    private static EntryNode parseEntry(JsonObject object, String path, ParseContext context) throws IOException {
        List<String> sources = new ArrayList<>(3);
        if (object.has("builtin"))
            sources.add("builtin");
        if (object.has("item"))
            sources.add("item");
        if (object.has("screen"))
            sources.add("screen");
        if (sources.size() != 1)
            throw error(context.source, path, "an entry needs exactly one of 'builtin', 'item' or 'screen'");

        String kind = sources.getFirst();
        String target = requiredString(object, kind, context.source, path);
        String method = kind.equals("screen")
                ? optionalString(object, "method", context.source, path, "create")
                : "";
        String title = optionalString(object, "title", context.source, path, "");
        String titleFallback = optionalString(object, "fallback", context.source, path, "");
        String description = optionalString(object, "description", context.source, path, "");
        String descriptionFallback = optionalString(object, "descriptionFallback", context.source, path, "");
        String color = optionalString(object, "color", context.source, path, "");
        validateColor(color, context.source, path + ".color", true);
        float size = boundedFloat(object.get("size"), context.source, path + ".size", 15, 1, 128);
        boolean showUnavailable = optionalBoolean(object, "showUnavailable", context.source, path, false);
        Style style = parseStyle(object, path, context, Dimension.wrapContent(), Dimension.wrapContent(),
                new Insets(16, 9, 16, 9), null, 48);
        return new EntryNode(kind, target, method, title, titleFallback, description, descriptionFallback, color,
                size, showUnavailable, style);
    }

    private static ButtonNode parseButton(JsonObject object, String path, ParseContext context) throws IOException {
        String text = requiredString(object, "text", context.source, path);
        String fallback = optionalString(object, "fallback", context.source, path, "");
        String rawAction = optionalString(object, "action", context.source, path, "close")
                .toUpperCase(Locale.ROOT);
        ButtonAction action;
        try {
            action = ButtonAction.valueOf(rawAction);
        } catch (IllegalArgumentException exception) {
            throw error(context.source, path + ".action", "unsupported action '" + rawAction + "'");
        }
        String color = optionalString(object, "color", context.source, path, "accent");
        validateColor(color, context.source, path + ".color", false);
        float size = boundedFloat(object.get("size"), context.source, path + ".size", 14, 1, 128);
        Style style = parseStyle(object, path, context, Dimension.wrapContent(), Dimension.wrapContent(),
                new Insets(16, 6, 16, 6), null, 36);
        return new ButtonNode(text, fallback, action, color, size, style);
    }

    private static Style parseStyle(JsonObject object, String path, ParseContext context, Dimension defaultWidth,
            Dimension defaultHeight, Insets defaultPadding, Integer defaultMinWidth, Integer defaultMinHeight)
            throws IOException {
        Dimension width = dimension(object.get("width"), context.source, path + ".width", defaultWidth);
        Dimension height = dimension(object.get("height"), context.source, path + ".height", defaultHeight);
        float weight = boundedFloat(object.get("weight"), context.source, path + ".weight", 0, 0, 1000);
        Insets margin = insets(object.get("margin"), context.source, path + ".margin", Insets.NONE);
        Insets padding = insets(object.get("padding"), context.source, path + ".padding", defaultPadding);
        String gravity = optionalString(object, "gravity", context.source, path, "");
        String background = optionalString(object, "background", context.source, path, "");
        validateGravity(gravity, context.source, path + ".gravity");
        validateColor(background, context.source, path + ".background", true);
        Integer minWidth = optionalBoundedInt(object.get("minWidth"), context.source, path + ".minWidth",
                defaultMinWidth, 0, 10000);
        Integer minHeight = optionalBoundedInt(object.get("minHeight"), context.source, path + ".minHeight",
                defaultMinHeight, 0, 10000);
        boolean visible = optionalBoolean(object, "visible", context.source, path, true);
        return new Style(width, height, weight, margin, padding, gravity, background, minWidth, minHeight, visible);
    }

    private static HubTheme parseTheme(JsonElement element, String source) throws IOException {
        if (element == null || element.isJsonNull())
            return HubTheme.defaults();
        JsonObject theme = object(element, source, "theme");
        HubTheme defaults = HubTheme.defaults();
        return new HubTheme(
                themeColor(theme, "background", source, defaults.background()),
                themeColor(theme, "surface", source, defaults.surface()),
                themeColor(theme, "surfaceAlt", source, defaults.surfaceAlt()),
                themeColor(theme, "accent", source, defaults.accent()),
                themeColor(theme, "muted", source, defaults.muted()),
                themeColor(theme, "outline", source, defaults.outline()),
                themeColor(theme, "disabledOutline", source, defaults.disabledOutline()));
    }

    private static int themeColor(JsonObject theme, String name, String source, int fallback) throws IOException {
        if (!theme.has(name) || theme.get(name).isJsonNull())
            return fallback;
        String value = optionalString(theme, name, source, "theme", "").trim();
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (!hex.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"))
            throw error(source, "theme." + name, "expected #RRGGBB or #AARRGGBB");
        if (hex.length() == 6)
            hex = "FF" + hex;
        return (int) Long.parseLong(hex, 16);
    }

    private static void validateColor(String value, String source, String path, boolean allowBlankOrNone)
            throws IOException {
        if (value == null || value.isBlank())
            return;
        if (allowBlankOrNone && value.equalsIgnoreCase("none"))
            return;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (switch (normalized) {
            case "background", "surface", "surfacealt", "accent", "muted", "outline", "disabledoutline" -> true;
            default -> normalized.matches("#?[0-9a-f]{6}([0-9a-f]{2})?");
        })
            return;
        throw error(source, path, "expected a theme colour name or #RRGGBB/#AARRGGBB");
    }

    private static void validateGravity(String value, String source, String path) throws IOException {
        if (value == null || value.isBlank())
            return;
        for (String part : value.split("[|,]")) {
            String normalized = part.trim().toLowerCase(Locale.ROOT);
            if (normalized.isEmpty())
                continue;
            if (!switch (normalized) {
                case "left", "start", "right", "end", "center_horizontal", "top", "bottom", "center_vertical",
                        "center" -> true;
                default -> false;
            })
                throw error(source, path, "unsupported gravity value '" + normalized + "'");
        }
    }

    private static Dimension dimension(JsonElement element, String source, String path, Dimension fallback)
            throws IOException {
        if (element == null || element.isJsonNull())
            return fallback;
        if (!element.isJsonPrimitive())
            throw error(source, path, "expected a number, 'wrap_content' or 'match_parent'");
        if (element.getAsJsonPrimitive().isNumber()) {
            int value = element.getAsInt();
            if (value < 0)
                throw error(source, path, "dp dimensions cannot be negative");
            return Dimension.dp(value);
        }
        String value = element.getAsString().trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "match", "match_parent" -> Dimension.matchParent();
            case "wrap", "wrap_content" -> Dimension.wrapContent();
            default -> {
                if (!value.endsWith("dp"))
                    throw error(source, path, "expected a number, 'wrap_content' or 'match_parent'");
                try {
                    int dp = Integer.parseInt(value.substring(0, value.length() - 2).trim());
                    if (dp < 0)
                        throw error(source, path, "dp dimensions cannot be negative");
                    yield Dimension.dp(dp);
                } catch (NumberFormatException exception) {
                    throw error(source, path, "invalid dp dimension '" + value + "'");
                }
            }
        };
    }

    private static Insets insets(JsonElement element, String source, String path, Insets fallback)
            throws IOException {
        if (element == null || element.isJsonNull())
            return fallback;
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            int all = nonNegativeInt(element, source, path);
            return new Insets(all, all, all, all);
        }
        if (!element.isJsonArray())
            throw error(source, path, "expected a number or an array of 1, 2 or 4 numbers");
        JsonArray values = element.getAsJsonArray();
        return switch (values.size()) {
            case 1 -> {
                int all = nonNegativeInt(values.get(0), source, path + "[0]");
                yield new Insets(all, all, all, all);
            }
            case 2 -> {
                int horizontal = nonNegativeInt(values.get(0), source, path + "[0]");
                int vertical = nonNegativeInt(values.get(1), source, path + "[1]");
                yield new Insets(horizontal, vertical, horizontal, vertical);
            }
            case 4 -> new Insets(
                    nonNegativeInt(values.get(0), source, path + "[0]"),
                    nonNegativeInt(values.get(1), source, path + "[1]"),
                    nonNegativeInt(values.get(2), source, path + "[2]"),
                    nonNegativeInt(values.get(3), source, path + "[3]"));
            default -> throw error(source, path, "expected an array of 1, 2 or 4 numbers");
        };
    }

    private static JsonObject object(JsonElement element, String source, String path) throws IOException {
        if (element == null || !element.isJsonObject())
            throw error(source, path, "expected a JSON object");
        return element.getAsJsonObject();
    }

    private static JsonArray array(JsonElement element, String source, String path) throws IOException {
        if (element == null || !element.isJsonArray())
            throw error(source, path, "expected a JSON array");
        return element.getAsJsonArray();
    }

    private static String requiredString(JsonObject object, String name, String source, String path)
            throws IOException {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
            throw error(source, path + "." + name, "expected a non-empty string");
        String value = element.getAsString().trim();
        if (value.isEmpty())
            throw error(source, path + "." + name, "expected a non-empty string");
        return value;
    }

    private static String optionalString(JsonObject object, String name, String source, String path, String fallback)
            throws IOException {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull())
            return fallback;
        if (!element.isJsonPrimitive())
            throw error(source, path + "." + name, "expected a string");
        return element.getAsString();
    }

    private static boolean optionalBoolean(JsonObject object, String name, String source, String path,
            boolean fallback) throws IOException {
        JsonElement element = object.get(name);
        if (element == null || element.isJsonNull())
            return fallback;
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean())
            throw error(source, path + "." + name, "expected a boolean");
        return element.getAsBoolean();
    }

    private static Orientation parseOrientation(String value, ParseContext context, String path) throws IOException {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "vertical", "column" -> Orientation.VERTICAL;
            case "horizontal", "row" -> Orientation.HORIZONTAL;
            default -> throw error(context.source, path + ".orientation",
                    "expected 'vertical' or 'horizontal', found '" + value + "'");
        };
    }

    private static int intValue(JsonElement element, String source, String path, int fallback) throws IOException {
        if (element == null || element.isJsonNull())
            return fallback;
        return integer(element, source, path);
    }

    private static int boundedInt(JsonElement element, String source, String path, int fallback, int min, int max)
            throws IOException {
        int value = intValue(element, source, path, fallback);
        if (value < min || value > max)
            throw error(source, path, "expected a value from " + min + " to " + max + ", found " + value);
        return value;
    }

    private static Integer optionalBoundedInt(JsonElement element, String source, String path, Integer fallback,
            int min, int max) throws IOException {
        if (element == null || element.isJsonNull())
            return fallback;
        int value = integer(element, source, path);
        if (value < min || value > max)
            throw error(source, path, "expected a value from " + min + " to " + max + ", found " + value);
        return value;
    }

    private static int nonNegativeInt(JsonElement element, String source, String path) throws IOException {
        int value = integer(element, source, path);
        if (value < 0)
            throw error(source, path, "expected a non-negative number");
        return value;
    }

    private static int integer(JsonElement element, String source, String path) throws IOException {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
            throw error(source, path, "expected an integer");
        try {
            return element.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException exception) {
            throw error(source, path, "expected an integer");
        }
    }

    private static float boundedFloat(JsonElement element, String source, String path, float fallback, float min,
            float max) throws IOException {
        if (element == null || element.isJsonNull())
            return fallback;
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber())
            throw error(source, path, "expected a number");
        float value = element.getAsFloat();
        if (!Float.isFinite(value) || value < min || value > max)
            throw error(source, path, "expected a value from " + min + " to " + max);
        return value;
    }

    private static IOException error(String source, String path, String message) {
        return new IOException(source + ": " + path + ": " + message);
    }

    private static final class ParseContext {
        private final String source;
        private int nodes;

        private ParseContext(String source) {
            this.source = source;
        }
    }

    private HubLayoutLoader() {
    }

    public record LoadResult(HubLayout layout, boolean usingFallback, String error) {
        public LoadResult {
            if (layout == null)
                throw new IllegalArgumentException("layout cannot be null");
            error = error == null ? "" : error;
        }
    }
}
