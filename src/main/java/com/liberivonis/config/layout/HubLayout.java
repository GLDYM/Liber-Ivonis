package com.liberivonis.config.layout;

import java.util.List;
import java.util.Objects;

/** Parsed, UI-independent description of a Hub layout and its palette. */
public record HubLayout(int schemaVersion, HubTheme theme, Node root, String source) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public HubLayout {
        if (schemaVersion != CURRENT_SCHEMA_VERSION)
            throw new IllegalArgumentException("Unsupported Hub layout schema version: " + schemaVersion);
        Objects.requireNonNull(theme, "theme");
        Objects.requireNonNull(root, "root");
        source = source == null ? "" : source;
    }

    public sealed interface Node permits ContainerNode, TextNode, EntryNode, ButtonNode, SpacerNode {
        Style style();
    }

    public enum ContainerType {
        LINEAR,
        SCROLL,
        GRID
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    public enum ButtonAction {
        CLOSE
    }

    public record ContainerNode(ContainerType type, Orientation orientation, int columns, int gap,
            boolean fillViewport, boolean smoothScrolling, List<Node> children, Style style) implements Node {
        public ContainerNode {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(orientation, "orientation");
            Objects.requireNonNull(style, "style");
            children = List.copyOf(children);
            if (columns < 1)
                throw new IllegalArgumentException("Grid columns must be at least 1");
            if (gap < 0)
                throw new IllegalArgumentException("Gap cannot be negative");
        }
    }

    public record TextNode(String text, String fallback, float textSize, String color, String gravity,
            int maxLines, Style style) implements Node {
        public TextNode {
            text = text == null ? "" : text;
            fallback = fallback == null ? "" : fallback;
            color = color == null ? "" : color;
            gravity = gravity == null ? "" : gravity;
            Objects.requireNonNull(style, "style");
            if (textSize <= 0)
                throw new IllegalArgumentException("Text size must be positive");
            if (maxLines < 0)
                throw new IllegalArgumentException("Max lines cannot be negative");
        }
    }

    public record EntryNode(String kind, String target, String method, String title, String titleFallback,
            String description, String descriptionFallback, String color, float textSize,
            boolean showUnavailable, Style style) implements Node {
        public EntryNode {
            kind = Objects.requireNonNull(kind, "kind");
            target = Objects.requireNonNull(target, "target");
            method = method == null ? "" : method;
            title = title == null ? "" : title;
            titleFallback = titleFallback == null ? "" : titleFallback;
            description = description == null ? "" : description;
            descriptionFallback = descriptionFallback == null ? "" : descriptionFallback;
            color = color == null ? "" : color;
            Objects.requireNonNull(style, "style");
            if (target.isBlank())
                throw new IllegalArgumentException("Entry target cannot be blank");
            if (textSize <= 0)
                throw new IllegalArgumentException("Text size must be positive");
        }
    }

    public record ButtonNode(String text, String fallback, ButtonAction action, String color, float textSize,
            Style style) implements Node {
        public ButtonNode {
            text = text == null ? "" : text;
            fallback = fallback == null ? "" : fallback;
            action = Objects.requireNonNull(action, "action");
            color = color == null ? "" : color;
            Objects.requireNonNull(style, "style");
            if (textSize <= 0)
                throw new IllegalArgumentException("Text size must be positive");
        }
    }

    public record SpacerNode(Style style) implements Node {
        public SpacerNode {
            Objects.requireNonNull(style, "style");
        }
    }

    public record Style(Dimension width, Dimension height, float weight, Insets margin, Insets padding,
            String gravity, String background, Integer minWidth, Integer minHeight, boolean visible) {
        public Style {
            Objects.requireNonNull(width, "width");
            Objects.requireNonNull(height, "height");
            Objects.requireNonNull(margin, "margin");
            Objects.requireNonNull(padding, "padding");
            gravity = gravity == null ? "" : gravity;
            background = background == null ? "" : background;
            if (!Float.isFinite(weight) || weight < 0)
                throw new IllegalArgumentException("Weight must be a finite, non-negative number");
            if (minWidth != null && minWidth < 0)
                throw new IllegalArgumentException("Minimum width cannot be negative");
            if (minHeight != null && minHeight < 0)
                throw new IllegalArgumentException("Minimum height cannot be negative");
        }

        public static Style root() {
            return new Style(Dimension.matchParent(), Dimension.matchParent(), 0, Insets.NONE, Insets.NONE,
                    "", "", null, null, true);
        }
    }

    public record Dimension(int value) {
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;

        public Dimension {
            if (value < WRAP_CONTENT)
                throw new IllegalArgumentException("Invalid layout dimension: " + value);
        }

        public static Dimension matchParent() {
            return new Dimension(MATCH_PARENT);
        }

        public static Dimension wrapContent() {
            return new Dimension(WRAP_CONTENT);
        }

        public static Dimension dp(int value) {
            if (value < 0)
                throw new IllegalArgumentException("Dimension must be a non-negative dp value");
            return new Dimension(value);
        }

    }

    public record Insets(int left, int top, int right, int bottom) {
        public static final Insets NONE = new Insets(0, 0, 0, 0);

        public Insets {
            if (left < 0 || top < 0 || right < 0 || bottom < 0)
                throw new IllegalArgumentException("Padding and margin values cannot be negative");
        }
    }
}
