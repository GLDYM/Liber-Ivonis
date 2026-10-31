package com.liberivonis.client.screen;

import com.liberivonis.client.HubLocalization;
import com.liberivonis.client.HubNavigation;
import com.liberivonis.config.HubConfigFiles;
import com.liberivonis.config.IvonisConfig;
import com.liberivonis.config.layout.HubLayoutLoader;
import com.liberivonis.config.layout.HubTheme;
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
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** ModernUI settings page for JSON layout management and gameplay settings. */
public final class IvonisConfigFragment extends Fragment {
    private static final Logger LOGGER = LoggerFactory.getLogger("LiberIvonis/ConfigScreen");
    private static final int ERROR_COLOR = 0xFFFF7777;
    private final Screen parent;

    public IvonisConfigFragment(Screen parent) {
        this.parent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, icyllis.modernui.util.DataSet state) {
        Context context = requireContext();
        HubLocalization localization = HubLocalization.current();
        HubLayoutLoader.LoadResult layoutResult = HubLayoutLoader.loadResult();
        HubColors colors = HubColors.from(layoutResult.layout().theme());

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(root.dp(24), root.dp(22), root.dp(24), root.dp(18));
        root.setBackground(new ColorDrawable(colors.background()));

        root.addView(text(context, localization, "screen.liber_ivonis.settings",
                "Liber Ivonis Settings", 22, colors.accent()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 3));
        root.addView(raw(context, HubConfigFiles.root().toString(), 12, colors.muted()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 14));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(section(context, localization, "screen.liber_ivonis.settings.layout",
                "Hub layout", colors));
        content.addView(raw(context, HubConfigFiles.layoutPath().toString(), 12, colors.accent()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 5));

        TextView status = raw(context, layoutStatus(localization, layoutResult), 12,
                layoutResult.usingFallback() ? ERROR_COLOR : colors.outline());
        status.setMaxLines(5);
        content.addView(status, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 10));

        content.addView(text(context, localization, "screen.liber_ivonis.settings.theme",
                "Theme", 13, colors.muted()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 4));
        List<View> swatches = new ArrayList<>(7);
        LinearLayout palette = new LinearLayout(context);
        palette.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(palette, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 12));
        addPaletteSwatches(context, palette, swatches, layoutResult.layout().theme());

        LinearLayout fileActions = new LinearLayout(context);
        fileActions.setOrientation(LinearLayout.HORIZONTAL);
        Button openFolder = button(context, localization, "screen.liber_ivonis.settings.open_folder",
                "Open config folder", colors);
        openFolder.setOnClickListener(view -> openPath(HubConfigFiles.root(), status, localization));
        Button editLayout = button(context, localization, "screen.liber_ivonis.settings.open_layout",
                "Edit hub.json", colors);
        editLayout.setOnClickListener(view -> openPath(HubConfigFiles.layoutPath(), status, localization));
        fileActions.addView(openFolder, weightedParams(0, 0, 0, 4));
        fileActions.addView(editLayout, weightedParams(4, 0, 0, 0));
        content.addView(fileActions, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 6));

        LinearLayout previewActions = new LinearLayout(context);
        previewActions.setOrientation(LinearLayout.HORIZONTAL);
        Button reload = button(context, localization, "screen.liber_ivonis.settings.reload",
                "Reload", colors);
        reload.setOnClickListener(view -> reload(localization, status, swatches));
        Button preview = button(context, localization, "screen.liber_ivonis.settings.preview",
                "Preview Hub", colors);
        preview.setOnClickListener(view -> HubNavigation.openModernScreen(new IvonisHubFragment(true)));
        previewActions.addView(reload, weightedParams(0, 0, 0, 4));
        previewActions.addView(preview, weightedParams(4, 0, 0, 0));
        content.addView(previewActions, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0,
                Minecraft.getInstance().player == null ? 4 : 14));
        if (Minecraft.getInstance().player == null) {
            content.addView(text(context, localization, "screen.liber_ivonis.settings.preview_note",
                    "Preview only: entries that need a world are disabled", 12, colors.muted()),
                    params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            0, 0, 0, 14));
        }

        content.addView(section(context, localization, "screen.liber_ivonis.settings.language",
                "Language overrides", colors));
        Path languagePath = HubConfigFiles.languagePath(
                Minecraft.getInstance().getLanguageManager().getSelected());
        content.addView(raw(context, languagePath.toString(), 12, colors.muted()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 5));
        Button openLanguage = button(context, localization, "screen.liber_ivonis.settings.open_language",
                "Open lang folder", colors);
        openLanguage.setOnClickListener(view -> openPath(HubConfigFiles.langDirectory(), status, localization));
        content.addView(openLanguage, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 14));

        content.addView(section(context, localization, "screen.liber_ivonis.settings.gameplay",
                "Gameplay", colors));
        CheckBox giveBook = new CheckBox(context);
        giveBook.setText(translate(localization, "screen.liber_ivonis.settings.give_book",
                "Give Liber Ivonis on first login"));
        giveBook.setTextSize(15);
        giveBook.setTextColor(colors.accent());
        giveBook.setChecked(IvonisConfig.giveBookOnStart.get());
        content.addView(giveBook, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 4));
        content.addView(text(context, localization, "screen.liber_ivonis.settings.server_only",
                "This option is a common setting and is used by the server", 12, colors.muted()),
                params(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        0, 0, 0, 8));

        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button back = button(context, localization, "screen.liber_ivonis.back", "Back", colors);
        back.setOnClickListener(
                view -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(parent)));
        Button save = button(context, localization, "screen.liber_ivonis.settings.save", "Save", colors);
        save.setOnClickListener(view -> Minecraft.getInstance().execute(() -> save(giveBook, parent)));
        actions.addView(back, weightedParams(0, 0, 0, 4));
        actions.addView(save, weightedParams(4, 0, 0, 0));
        root.addView(actions, params(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 14, 0, 0));
        return root;
    }

    private static void reload(HubLocalization localization, TextView status, List<View> swatches) {
        HubLayoutLoader.LoadResult result = HubLayoutLoader.loadResult();
        status.setText(layoutStatus(localization, result));
        status.setTextColor(result.usingFallback() ? ERROR_COLOR : swatchColor(result.layout().theme()));
        applyPalette(swatches, result.layout().theme());
    }

    private static String layoutStatus(HubLocalization localization, HubLayoutLoader.LoadResult result) {
        String status = result.usingFallback()
                ? translate(localization, "screen.liber_ivonis.settings.layout_invalid",
                        "Layout is invalid; the bundled default is active")
                : translate(localization, "screen.liber_ivonis.settings.layout_valid",
                        "Layout loaded successfully");
        return result.error().isBlank() ? status : status + "\n" + result.error();
    }

    private static void openPath(Path path, TextView status, HubLocalization localization) {
        if (status == null)
            return;
        try {
            HubConfigFiles.ensureDefaults();
            Util.getPlatform().openPath(path);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to open config path {}", path, exception);
            status.setText(translate(localization, "screen.liber_ivonis.settings.open_failed",
                    "Could not open the configuration path") + "\n" + path);
            status.setTextColor(ERROR_COLOR);
        }
    }

    private static void save(CheckBox giveBook, Screen parent) {
        IvonisConfig.giveBookOnStart.set(giveBook.isChecked());
        IvonisConfig.COMMON_SPEC.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private static void addPaletteSwatches(Context context, LinearLayout row, List<View> swatches, HubTheme theme) {
        int[] colors = palette(theme);
        for (int index = 0; index < colors.length; index++) {
            View swatch = new View(context);
            swatch.setBackground(new ColorDrawable(colors[index]));
            swatches.add(swatch);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, row.dp(18), 1);
            params.setMargins(index == 0 ? 0 : row.dp(2), 0, index == colors.length - 1 ? 0 : row.dp(2), 0);
            row.addView(swatch, params);
        }
    }

    private static void applyPalette(List<View> swatches, HubTheme theme) {
        int[] colors = palette(theme);
        for (int index = 0; index < Math.min(swatches.size(), colors.length); index++)
            swatches.get(index).setBackground(new ColorDrawable(colors[index]));
    }

    private static int[] palette(HubTheme theme) {
        return new int[] { theme.background(), theme.surface(), theme.surfaceAlt(), theme.accent(), theme.muted(),
                theme.outline(), theme.disabledOutline() };
    }

    private static int swatchColor(HubTheme theme) {
        return theme.outline();
    }

    private static TextView section(Context context, HubLocalization localization, String key, String fallback,
            HubColors colors) {
        TextView view = text(context, localization, key, fallback, 15, colors.accent());
        view.setPadding(0, view.dp(2), 0, view.dp(6));
        return view;
    }

    private static Button button(Context context, HubLocalization localization, String key, String fallback,
            HubColors colors) {
        Button button = new Button(context);
        button.setText(translate(localization, key, fallback));
        button.setTextSize(14);
        button.setTextColor(colors.accent());
        button.setGravity(Gravity.CENTER);
        button.setPadding(button.dp(12), button.dp(9), button.dp(12), button.dp(9));
        button.setBackground(cardBackground(colors));
        return button;
    }

    private static TextView text(Context context, HubLocalization localization, String key, String fallback,
            float size, int color) {
        return raw(context, translate(localization, key, fallback), size, color);
    }

    private static TextView raw(Context context, String value, float size, int color) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private static String translate(HubLocalization localization, String key, String fallback) {
        return localization.resolve(key, fallback);
    }

    private static ShapeDrawable cardBackground(HubColors colors) {
        ShapeDrawable shape = new ShapeDrawable();
        shape.setShape(ShapeDrawable.RECTANGLE);
        shape.setCornerRadius(8);
        shape.setColor(colors.surfaceAlt());
        shape.setStroke(1, colors.outline());
        return shape;
    }

    private static LinearLayout.LayoutParams weightedParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private static LinearLayout.LayoutParams params(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        return params;
    }
}
