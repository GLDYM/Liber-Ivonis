package com.liberivonis.client.screen;

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

import java.util.List;

/** Reusable two-column ModernUI page for one handbook family. */
public final class HandbookCategoryFragment extends Fragment {
    private static final int BG = 0xFF0D1117, SURFACE = 0xFF202938, ACCENT = 0xFFE8D9B5, MUTED = 0xFF9AA6B8, OUTLINE = 0xFF364255;
    private final Component title;
    private final List<HandbookEntry> entries;

    public HandbookCategoryFragment(Component title, List<HandbookEntry> entries) { this.title = title; this.entries = entries; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, icyllis.modernui.util.DataSet state) {
        Context context = requireContext();
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(root.dp(24), root.dp(22), root.dp(24), root.dp(18));
        root.setBackground(new ColorDrawable(BG));
        TextView heading = new TextView(context); heading.setText(title.getString()); heading.setTextSize(22); heading.setTextColor(ACCENT);
        root.addView(heading, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 16));
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true); scroll.setSmoothScrollingEnabled(true);
        LinearLayout list = new LinearLayout(context); list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < entries.size(); i++) {
            if ((i & 1) == 0) { row = new LinearLayout(context); row.setOrientation(LinearLayout.HORIZONTAL); list.addView(row, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 8)); }
            HandbookEntry entry = entries.get(i); Button button = new Button(context);
            button.setText(entry.title().getString()); button.setTextSize(15); button.setTextColor(ACCENT); button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL); button.setPadding(button.dp(16), button.dp(9), button.dp(16), button.dp(9)); button.setMinHeight(button.dp(48)); button.setBackground(card());
            button.setOnClickListener(view -> HubNavigation.openAction(entry.action()));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); bp.setMargins((i & 1) == 0 ? 0 : 4, 0, (i & 1) == 0 ? 4 : 0, 0); row.addView(button, bp);
        }
        scroll.addView(list, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)); root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        Button close = new Button(context); close.setText(Component.translatable("screen.liber_ivonis.back").getString()); close.setTextColor(ACCENT); close.setBackground(card()); close.setOnClickListener(v -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null))); root.addView(close, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 14, 0, 0));
        return root;
    }
    private static ShapeDrawable card() { ShapeDrawable d = new ShapeDrawable(); d.setShape(ShapeDrawable.RECTANGLE); d.setCornerRadius(8); d.setColor(SURFACE); d.setStroke(1, OUTLINE); return d; }
    private static LinearLayout.LayoutParams params(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.setMargins(l,t,r,b); return p; }
}
