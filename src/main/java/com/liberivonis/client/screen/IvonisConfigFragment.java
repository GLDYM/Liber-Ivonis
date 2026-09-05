package com.liberivonis.client.screen;

import com.liberivonis.config.IvonisConfig;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.CheckBox;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** ModernUI settings page for the Hub and its client-side theme. */
public final class IvonisConfigFragment extends Fragment {
    private static final String[] COLOR_NAMES = { "background", "surface", "surfaceAlt", "accent", "muted",
            "outline", "disabledOutline" };
    private final Screen parent;

    public IvonisConfigFragment(Screen parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, icyllis.modernui.util.DataSet state) {
        Context context = requireContext();
        HubColors colors = HubColors.current();
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(root.dp(24), root.dp(22), root.dp(24), root.dp(18));
        root.setBackground(new ColorDrawable(colors.background()));

        TextView title = text(context, "screen.liber_ivonis.settings", 22, colors.accent());
        root.addView(title, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 12));

        ScrollView scroll = new ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox giveBook = new CheckBox(context);
        giveBook.setText(Component.translatable("screen.liber_ivonis.settings.give_book").getString());
        giveBook.setTextSize(15);
        giveBook.setTextColor(colors.accent());
        giveBook.setChecked(IvonisConfig.giveBookOnStart.get());
        content.addView(giveBook, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 0, 0, 6));
        TextView serverNote = text(context, "screen.liber_ivonis.settings.server_only", 12, colors.muted());
        content.addView(serverNote, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 0, 0, 12));

        Map<String, EditText> fields = new LinkedHashMap<>();
        for (String name : COLOR_NAMES) {
            TextView label = text(context, "hub.color." + name, 13, colors.muted());
            content.addView(label, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    0, 6, 0, 2));
            EditText input = new EditText(context);
            input.setSingleLine(true);
            input.setText(colorValue(name));
            input.setTextSize(14);
            input.setTextColor(colors.accent());
            input.setHintTextColor(colors.muted());
            input.setPadding(input.dp(12), input.dp(8), input.dp(12), input.dp(8));
            input.setBackground(fieldBackground(colors));
            content.addView(input, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    0, 0, 0, 4));
            fields.put(name, input);
        }

        TextView status = text(context, "", 12, 0xFFFF7777);
        content.addView(status, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0, 8, 0, 4));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button back = button(context, "screen.liber_ivonis.back", colors);
        back.setOnClickListener(view -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(parent)));
        Button save = button(context, "screen.liber_ivonis.settings.save", colors);
        save.setOnClickListener(view -> save(fields, giveBook, status, parent));
        actions.addView(back, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        saveParams.setMargins(actions.dp(4), 0, 0, 0);
        actions.addView(save, saveParams);
        root.addView(actions, params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0, 14, 0, 0));
        return root;
    }

    private static void save(Map<String, EditText> fields, CheckBox giveBook, TextView status, Screen parent) {
        for (Map.Entry<String, EditText> entry : fields.entrySet()) {
            String value = entry.getValue().getText().toString().trim();
            if (!value.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
                status.setText(Component.translatable("screen.liber_ivonis.settings.invalid_color").getString());
                return;
            }
            setColorValue(entry.getKey(), value);
        }
        IvonisConfig.giveBookOnStart.set(giveBook.isChecked());
        IvonisConfig.CLIENT_SPEC.save();
        IvonisConfig.COMMON_SPEC.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private static String colorValue(String name) {
        return switch (name) {
            case "background" -> IvonisConfig.hubBackground.get();
            case "surface" -> IvonisConfig.hubSurface.get();
            case "surfaceAlt" -> IvonisConfig.hubSurfaceAlt.get();
            case "accent" -> IvonisConfig.hubAccent.get();
            case "muted" -> IvonisConfig.hubMuted.get();
            case "outline" -> IvonisConfig.hubOutline.get();
            case "disabledOutline" -> IvonisConfig.hubDisabledOutline.get();
            default -> "#FFFFFFFF";
        };
    }

    private static void setColorValue(String name, String value) {
        switch (name) {
            case "background" -> IvonisConfig.hubBackground.set(value);
            case "surface" -> IvonisConfig.hubSurface.set(value);
            case "surfaceAlt" -> IvonisConfig.hubSurfaceAlt.set(value);
            case "accent" -> IvonisConfig.hubAccent.set(value);
            case "muted" -> IvonisConfig.hubMuted.set(value);
            case "outline" -> IvonisConfig.hubOutline.set(value);
            case "disabledOutline" -> IvonisConfig.hubDisabledOutline.set(value);
        }
    }

    private static Button button(Context context, String key, HubColors colors) {
        Button button = new Button(context);
        button.setText(Component.translatable(key).getString());
        button.setTextSize(14);
        button.setTextColor(colors.accent());
        button.setGravity(Gravity.CENTER);
        button.setPadding(button.dp(12), button.dp(9), button.dp(12), button.dp(9));
        button.setBackground(cardBackground(colors));
        return button;
    }

    private static TextView text(Context context, String key, float size, int color) {
        TextView view = new TextView(context);
        view.setText(Component.translatable(key).getString());
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private static ShapeDrawable cardBackground(HubColors colors) {
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(colors.surfaceAlt());
        shape.setStroke(1, colors.outline());
        return shape;
    }

    private static ShapeDrawable fieldBackground(HubColors colors) {
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(6);
        shape.setColor(colors.surface());
        shape.setStroke(1, colors.outline());
        return shape;
    }

    private static LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        return params;
    }
}
