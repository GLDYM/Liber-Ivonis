package com.liberivonis.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class IvonisConfig {
    public static final ModConfigSpec SPEC;
    /** One ordered list containing built-ins, item handbooks and custom screens. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> hubEntries;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("hub");
        hubEntries = b.comment(
                "Hub entries in display order. Built-in: id|category. Handbook: item:modid:item_id|title|category. Screen: screen:title|class|method|category")
                .defineListAllowEmpty("entries",
                        List.of("ftbquests", "patchouli", "modonomicon", "guideme", "ageratum", "inventory",
                                "advancement", "controls"),
                        v -> v instanceof String && !((String) v).isBlank());
        b.pop();
        SPEC = b.build();
    }

    private IvonisConfig() {
    }
}
