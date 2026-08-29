package com.liberivonis.registry;

import com.liberivonis.LiberIvonis;
import com.liberivonis.item.IvonisBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LiberIvonis.MOD_ID);
    public static final DeferredItem<Item> IVONIS_BOOK = ITEMS.register("ivonis_book", () -> new IvonisBookItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
