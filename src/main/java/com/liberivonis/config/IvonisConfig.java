package com.liberivonis.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IvonisConfig {
    public static final ModConfigSpec COMMON_SPEC;
    /** Whether each player should receive the book once on their first login. */
    public static final ModConfigSpec.ConfigValue<Boolean> giveBookOnStart;

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        commonBuilder.push("gameplay");
        giveBookOnStart = commonBuilder.comment("Give each player one Liber Ivonis on their first login.")
                .define("giveBookOnStart", true);
        commonBuilder.pop();
        COMMON_SPEC = commonBuilder.build();
    }

    private IvonisConfig() {
    }
}
