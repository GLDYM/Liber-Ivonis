package com.liberivonis.client.screen;

import com.liberivonis.integration.HubEntries;
import com.liberivonis.integration.HubEntry;
import com.liberivonis.client.HubNavigation;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** The Hub UI is a ModernUI View tree rendered by ModernUI's Arc3D backend. */
public final class IvonisHubFragment extends Fragment {
    private static final int BG = 0xFF0D1117;
    private static final int SURFACE = 0xFF171D27;
    private static final int SURFACE_ALT = 0xFF202938;
    private static final int ACCENT = 0xFFE8D9B5;
    private static final int MUTED = 0xFF9AA6B8;
    private static final int OUTLINE = 0xFF364255;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, icyllis.modernui.util.DataSet savedInstanceState) {
        Context context = requireContext();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(root, 24), dp(root, 22), dp(root, 24), dp(root, 18));
        root.setBackground(new ColorDrawable(BG));

        TextView title = text(context, Component.translatable("screen.liber_ivonis.hub"), 22, ACCENT);
        root.addView(title, marginParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 3));

        TextView subtitle = text(context, Component.translatable("screen.liber_ivonis.subtitle"), 13, MUTED);
        root.addView(subtitle, marginParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 16));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setSmoothScrollingEnabled(true);
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        java.util.List<LinearLayout> rows = new java.util.ArrayList<>();
        scroll.addView(list, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        java.util.List<HubEntry> entries = HubEntries.collect();
        String category = null;
        LinearLayout row = null;
        int column = 0;
        Button previous = null;
        for (int index = 0; index < entries.size(); index++) {
            HubEntry entry = entries.get(index);
            if (!entry.category().equals(category)) {
                category = entry.category();
                column = 0;
                previous = null;
                if (!category.isEmpty()) {
                    TextView heading = text(context, categoryTitle(category), 15, ACCENT);
                    list.addView(heading, marginParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2, 8, 0, 6));
                }
            }
            if (column == 0) {
                row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                list.addView(row, marginParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 8));
                rows.add(row);
            }

            Button button = new Button(context);
            // ModernUI TextView consumes CharSequence; the Component is resolved
            // through Minecraft's active language before reaching the view tree.
            button.setText(entry.title().getString());
            button.setTextSize(15);
            button.setTextColor(entry.available() ? ACCENT : MUTED);
            button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            button.setPadding(dp(button, 16), dp(button, 9), dp(button, 16), dp(button, 9));
            button.setMinHeight(dp(button, 48));
            button.setBackground(cardBackground(entry.available()));
            button.setOnClickListener(view -> open(entry));
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            buttonParams.setMargins(column == 0 ? 0 : 4, 0, column == 0 ? 4 : 0, 0);
            row.addView(button, buttonParams);
            previous = button;
            column++;
            if (column == 2) {
                column = 0;
                previous = null;
            }
        }

        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        root.post(() -> {
            for (LinearLayout itemRow : rows) {
                int height = 0;
                for (int i = 0; i < itemRow.getChildCount(); i++) height = Math.max(height, itemRow.getChildAt(i).getMeasuredHeight());
                if (height <= 0) continue;
                itemRow.getLayoutParams().height = height;
                for (int i = 0; i < itemRow.getChildCount(); i++) {
                    View child = itemRow.getChildAt(i);
                    child.getLayoutParams().height = height;
                    child.requestLayout();
                }
                itemRow.requestLayout();
            }
        });

        Button close = new Button(context);
        close.setText(Component.translatable("screen.liber_ivonis.back").getString());
        close.setTextSize(14);
        close.setTextColor(ACCENT);
        close.setBackground(cardBackground(true));
        close.setOnClickListener(view -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null)));
        root.addView(close, marginParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 14, 0, 0));
        // Use ordinary parent measurement. A fixed-width child inside a full-screen
        // ModernUI root can produce invalid GL viewport/scissor measurements on some
        // drivers. The horizontal padding provides the visual content width while
        // ModernUI retains control of GUI scaling.
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return root;
    }

    private static void open(HubEntry entry) {
        Minecraft.getInstance().execute(() -> {
            if (entry.id().equals("patchouli") || entry.id().equals("modonomicon") || entry.id().equals("guideme") || entry.id().equals("ageratum")) {
                entry.action().run();
            } else {
                HubNavigation.openAction(entry.action());
            }
        });
    }

    private static TextView text(Context context, Component value, float size, int color) {
        TextView view = new TextView(context);
        view.setText(value.getString());
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private static Component categoryTitle(String category) {
        if (category.equals("builtin") || category.equals("custom") || category.equals("handbooks")) {
            return Component.translatable("screen.liber_ivonis.category." + category);
        }
        // Custom categories may use an explicit translation key: key:my.mod.category.
        if (category.startsWith("key:") && category.length() > 4) {
            return Component.translatable(category.substring(4));
        }
        return Component.literal(category);
    }

    private static ShapeDrawable cardBackground(boolean enabled) {
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(enabled ? SURFACE_ALT : SURFACE);
        shape.setStroke(1, enabled ? OUTLINE : 0xFF252C38);
        return shape;
    }

    private static LinearLayout.LayoutParams marginParams(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private static int dp(View view, float value) { return view.dp(value); }
}
