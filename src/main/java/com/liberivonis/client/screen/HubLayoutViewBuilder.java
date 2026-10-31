package com.liberivonis.client.screen;

import com.liberivonis.client.HubLocalization;
import com.liberivonis.client.HubNavigation;
import com.liberivonis.config.layout.HubLayout;
import com.liberivonis.config.layout.HubLayout.ButtonNode;
import com.liberivonis.config.layout.HubLayout.ContainerNode;
import com.liberivonis.config.layout.HubLayout.EntryNode;
import com.liberivonis.config.layout.HubLayout.Insets;
import com.liberivonis.config.layout.HubLayout.Node;
import com.liberivonis.config.layout.HubLayout.Orientation;
import com.liberivonis.config.layout.HubLayout.SpacerNode;
import com.liberivonis.config.layout.HubLayout.Style;
import com.liberivonis.config.layout.HubLayout.TextNode;
import com.liberivonis.integration.HubEntries;
import com.liberivonis.integration.HubEntry;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns a validated JSON layout into a hierarchy of ModernUI views. */
final class HubLayoutViewBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/HubLayoutView");
    private final Context context;
    private final HubColors colors;
    private final HubLocalization localization;
    private final Map<String, HubEntry> builtins;
    private final boolean previewMode;

    HubLayoutViewBuilder(Context context, HubLayout layout, boolean previewMode) {
        this.context = context;
        this.colors = HubColors.from(layout.theme());
        this.localization = HubLocalization.current();
        this.builtins = HubEntries.builtins();
        this.previewMode = previewMode;
    }

    View build(HubLayout layout) {
        View root = buildNode(layout.root());
        if (root == null) {
            LOGGER.warn("Hub layout '{}' produced no visible root view", layout.source());
            root = new View(context);
        }
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return root;
    }

    private View buildNode(Node node) {
        if (node instanceof ContainerNode container)
            return buildContainer(container);
        if (node instanceof TextNode text)
            return buildText(text);
        if (node instanceof EntryNode entry)
            return buildEntry(entry);
        if (node instanceof ButtonNode button)
            return buildButton(button);
        if (node instanceof SpacerNode spacer)
            return buildSpacer(spacer);
        throw new IllegalStateException("Unsupported Hub layout node " + node.getClass().getName());
    }

    private View buildContainer(ContainerNode node) {
        return switch (node.type()) {
            case LINEAR -> buildLinear(node);
            case SCROLL -> buildScroll(node);
            case GRID -> buildGrid(node);
        };
    }

    private LinearLayout buildLinear(ContainerNode node) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(node.orientation() == Orientation.VERTICAL
                ? LinearLayout.VERTICAL
                : LinearLayout.HORIZONTAL);
        applyStyle(layout, node.style(), background(node.style().background(), false));
        applyGravity(layout, node.style().gravity());
        addChildren(layout, node.children(), node.gap());
        return layout;
    }

    private ScrollView buildScroll(ContainerNode node) {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(node.fillViewport());
        scroll.setSmoothScrollingEnabled(node.smoothScrolling());
        applyStyle(scroll, node.style(), background(node.style().background(), false));
        applyGravity(scroll, node.style().gravity());

        List<BuiltNode> children = buildChildren(node.children());
        if (children.isEmpty()) {
            scroll.addView(new View(context),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        } else if (children.size() == 1) {
            BuiltNode child = children.getFirst();
            scroll.addView(child.view(), layoutParams(scroll, child.node().style(), 0));
        } else {
            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < children.size(); i++) {
                BuiltNode child = children.get(i);
                addView(content, child.view(), child.node().style(), i == 0 ? 0 : node.gap());
            }
            scroll.addView(content, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return scroll;
    }

    private LinearLayout buildGrid(ContainerNode node) {
        LinearLayout grid = new LinearLayout(context);
        grid.setOrientation(LinearLayout.VERTICAL);
        applyStyle(grid, node.style(), background(node.style().background(), false));
        applyGravity(grid, node.style().gravity());

        List<BuiltNode> children = buildChildren(node.children());
        int columns = Math.max(1, node.columns());
        int rows = (children.size() + columns - 1) / columns;
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBaselineAligned(false);
            for (int column = 0; column < columns; column++) {
                int childIndex = rowIndex * columns + column;
                if (childIndex < children.size()) {
                    BuiltNode child = children.get(childIndex);
                    row.addView(child.view(), gridCellParams(row, child.node().style(), column, columns,
                            node.gap()));
                } else {
                    View placeholder = new View(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                    params.setMargins(column == 0 ? 0 : node.gap() / 2, 0,
                            column == columns - 1 ? 0 : node.gap() / 2, 0);
                    row.addView(placeholder, params);
                }
            }
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, rowIndex == rows - 1 ? 0 : node.gap());
            grid.addView(row, rowParams);
            equalizeRowHeight(row);
        }
        return grid;
    }

    private TextView buildText(TextNode node) {
        TextView text = new TextView(context);
        text.setText(localization.resolve(node.text(), node.fallback()));
        text.setTextSize(node.textSize());
        text.setTextColor(color(node.color(), colors.accent()));
        text.setGravity(gravity(node.gravity(), Gravity.LEFT));
        if (node.maxLines() > 0) {
            text.setMaxLines(node.maxLines());
            if (node.maxLines() == 1)
                text.setSingleLine();
        }
        applyStyle(text, node.style(), background(node.style().background(), false));
        return text;
    }

    private View buildEntry(EntryNode node) {
        HubEntry entry = switch (node.kind()) {
            case "builtin" -> builtins.get(node.target());
            case "item" -> HubEntries.item(node.target(), node.title(), node.titleFallback(),
                    node.description(), node.descriptionFallback()).orElse(null);
            case "screen" -> HubEntries.screen(node.target(), node.method(), node.title(), node.titleFallback(),
                    node.description(), node.descriptionFallback());
            default -> throw new IllegalStateException("Unsupported entry kind " + node.kind());
        };
        if (entry == null) {
            LOGGER.warn("Hub layout references unknown built-in entry '{}'", node.target());
            return null;
        }
        boolean available = entry.available() && (!entry.requiresWorld() || inWorld());
        if (!available && !node.showUnavailable() && !previewMode)
            return null;

        String title = node.title().isBlank()
                ? localization.resolve(entry.titleKey(), entry.titleFallback())
                : localization.resolve(node.title(), node.titleFallback());
        Button button = new Button(context);
        button.setText(title);
        button.setTextSize(node.textSize());
        button.setTextColor(color(node.color(), available ? colors.accent() : colors.muted()));
        button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        button.setEnabled(available);
        String description = node.description().isBlank()
                ? localization.resolve(entry.descriptionKey(), entry.descriptionFallback())
                : localization.resolve(node.description(), node.descriptionFallback());
        if (!description.isBlank())
            button.setContentDescription(description);
        if (available)
            button.setOnClickListener(view -> open(entry));

        applyStyle(button, node.style(), node.style().background().isBlank()
                ? cardBackground(available)
                : background(node.style().background(), true));
        return button;
    }

    private Button buildButton(ButtonNode node) {
        Button button = new Button(context);
        button.setText(localization.resolve(node.text(), node.fallback()));
        button.setTextSize(node.textSize());
        button.setTextColor(color(node.color(), colors.accent()));
        if (node.action() == HubLayout.ButtonAction.CLOSE) {
            button.setOnClickListener(view -> HubNavigation.closeModernScreen());
        }
        applyStyle(button, node.style(), node.style().background().isBlank()
                ? cardBackground(true)
                : background(node.style().background(), true));
        return button;
    }

    private View buildSpacer(SpacerNode node) {
        View spacer = new View(context);
        applyStyle(spacer, node.style(), background(node.style().background(), false));
        return spacer;
    }

    private void open(HubEntry entry) {
        if (!entry.available() || entry.requiresWorld() && !inWorld())
            return;
        Minecraft.getInstance().execute(() -> {
            if (entry.id().equals("patchouli") || entry.id().equals("modonomicon")
                    || entry.id().equals("guideme") || entry.id().equals("ageratum")) {
                entry.action().run();
            } else {
                HubNavigation.openAction(entry.action());
            }
        });
    }

    private List<BuiltNode> buildChildren(List<Node> nodes) {
        List<BuiltNode> children = new ArrayList<>(nodes.size());
        for (Node child : nodes) {
            View view = buildNode(child);
            if (view != null)
                children.add(new BuiltNode(child, view));
        }
        return children;
    }

    private void addChildren(ViewGroup parent, List<Node> nodes, int gap) {
        int added = 0;
        for (Node child : nodes) {
            View view = buildNode(child);
            if (view == null)
                continue;
            addView(parent, view, child.style(), added == 0 ? 0 : gap);
            added++;
        }
    }

    private void addView(ViewGroup parent, View child, Style style, int gapBefore) {
        parent.addView(child, layoutParams(parent, style, gapBefore));
    }

    private ViewGroup.LayoutParams layoutParams(ViewGroup parent, Style style, int gapBefore) {
        int width = dimension(parent, style.width().value());
        int height = dimension(parent, style.height().value());
        Insets margins = style.margin();

        if (parent instanceof LinearLayout linear) {
            boolean vertical = linear.getOrientation() == LinearLayout.VERTICAL;
            if (style.weight() > 0) {
                if (vertical)
                    height = 0;
                else
                    width = 0;
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, style.weight());
            int layoutGravity = gravity(style.gravity(), 0);
            if (layoutGravity != 0)
                params.gravity = layoutGravity;
            params.setMargins(
                    dp(parent, margins.left()) + (vertical ? 0 : gapBefore),
                    dp(parent, margins.top()) + (vertical ? gapBefore : 0),
                    dp(parent, margins.right()),
                    dp(parent, margins.bottom()));
            return params;
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        int layoutGravity = gravity(style.gravity(), 0);
        if (layoutGravity != 0)
            params.gravity = layoutGravity;
        params.setMargins(dp(parent, margins.left()), dp(parent, margins.top()), dp(parent, margins.right()),
                dp(parent, margins.bottom()));
        return params;
    }

    private LinearLayout.LayoutParams gridCellParams(ViewGroup parent, Style style, int column, int columns,
            int gap) {
        int height = dimension(parent, style.height().value());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1);
        int layoutGravity = gravity(style.gravity(), 0);
        if (layoutGravity != 0)
            params.gravity = layoutGravity;
        Insets margins = style.margin();
        params.setMargins(
                dp(parent, margins.left()) + (column == 0 ? 0 : gap / 2),
                dp(parent, margins.top()),
                dp(parent, margins.right()) + (column == columns - 1 ? 0 : gap / 2),
                dp(parent, margins.bottom()));
        return params;
    }

    private void applyStyle(View view, Style style, Drawable background) {
        if (background != null)
            view.setBackground(background);
        else if (!style.background().isBlank() && style.background().equalsIgnoreCase("none"))
            view.setBackground(null);
        view.setPadding(
                dp(view, style.padding().left()), dp(view, style.padding().top()),
                dp(view, style.padding().right()), dp(view, style.padding().bottom()));
        if (style.minWidth() != null)
            view.setMinimumWidth(dp(view, style.minWidth()));
        if (style.minHeight() != null)
            view.setMinimumHeight(dp(view, style.minHeight()));
        view.setVisibility(style.visible() ? View.VISIBLE : View.GONE);
    }

    private void applyGravity(View view, String value) {
        int resolved = gravity(value, 0);
        if (resolved == 0)
            return;
        if (view instanceof LinearLayout layout)
            layout.setGravity(resolved);
        else if (view instanceof TextView text)
            text.setGravity(resolved);
        else if (view instanceof FrameLayout frame)
            frame.setForegroundGravity(resolved);
    }

    private void equalizeRowHeight(LinearLayout row) {
        row.post(() -> {
            int height = 0;
            for (int i = 0; i < row.getChildCount(); i++)
                height = Math.max(height, row.getChildAt(i).getMeasuredHeight());
            if (height <= 0)
                return;
            row.getLayoutParams().height = height;
            for (int i = 0; i < row.getChildCount(); i++) {
                View child = row.getChildAt(i);
                child.getLayoutParams().height = height;
                child.requestLayout();
            }
            row.requestLayout();
        });
    }

    private Drawable background(String name, boolean card) {
        if (name == null || name.isBlank())
            return null;
        if (name.equalsIgnoreCase("none"))
            return null;
        int fill = color(name, colors.surface());
        if (!card)
            return new ColorDrawable(fill);
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(fill);
        shape.setStroke(1, colors.outline());
        return shape;
    }

    private ShapeDrawable cardBackground(boolean enabled) {
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(enabled ? colors.surfaceAlt() : colors.surface());
        shape.setStroke(1, enabled ? colors.outline() : colors.disabledOutline());
        return shape;
    }

    private int color(String value, int fallback) {
        if (value == null || value.isBlank())
            return fallback;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "background" -> colors.background();
            case "surface" -> colors.surface();
            case "surfacealt" -> colors.surfaceAlt();
            case "accent" -> colors.accent();
            case "muted" -> colors.muted();
            case "outline" -> colors.outline();
            case "disabledoutline" -> colors.disabledOutline();
            default -> parseHexColor(value, fallback);
        };
    }

    private static int parseHexColor(String value, int fallback) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (!hex.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"))
            return fallback;
        if (hex.length() == 6)
            hex = "FF" + hex;
        return (int) Long.parseLong(hex, 16);
    }

    private static int gravity(String value, int fallback) {
        if (value == null || value.isBlank())
            return fallback;
        int gravity = 0;
        for (String part : value.split("[|,]")) {
            switch (part.trim().toLowerCase(Locale.ROOT)) {
                case "left", "start" -> gravity |= Gravity.LEFT;
                case "right", "end" -> gravity |= Gravity.RIGHT;
                case "center_horizontal" -> gravity |= Gravity.CENTER_HORIZONTAL;
                case "top" -> gravity |= Gravity.TOP;
                case "bottom" -> gravity |= Gravity.BOTTOM;
                case "center_vertical" -> gravity |= Gravity.CENTER_VERTICAL;
                case "center" -> gravity |= Gravity.CENTER;
                default -> {
                }
            }
        }
        return gravity == 0 ? fallback : gravity;
    }

    private static int dp(View view, int value) {
        return value == 0 ? 0 : view.dp(value);
    }

    private static int dimension(View view, int value) {
        return value < 0 ? value : dp(view, value);
    }

    private static boolean inWorld() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.level != null;
    }

    private record BuiltNode(Node node, View view) {
    }
}
