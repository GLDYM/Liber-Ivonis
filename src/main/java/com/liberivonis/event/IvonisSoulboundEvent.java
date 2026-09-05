package com.liberivonis.event;

import com.liberivonis.LiberIvonis;
import com.liberivonis.config.IvonisConfig;
import com.liberivonis.registry.ModItems;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LiberIvonis.MOD_ID)
public final class IvonisSoulboundEvent {
    private static final String KEY = "LiberIvonisSoulboundBooks";
    private static final ResourceLocation START_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(
            LiberIvonis.MOD_ID, "receive_ivonis_book");

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!IvonisConfig.giveBookOnStart.get() || !(event.getEntity() instanceof ServerPlayer player)
                || player.getServer() == null)
            return;
        AdvancementHolder advancement = player.getServer().getAdvancements().get(START_ADVANCEMENT);
        if (advancement != null)
            player.getAdvancements().award(advancement, "login");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer)
            return;
        List<ItemStack> keep = new ArrayList<>();
        for (ItemEntity drop : event.getDrops())
            if (drop.getItem().is(ModItems.IVONIS_BOOK.get())) {
                keep.add(drop.getItem().copy());
            }
        if (keep.isEmpty())
            return;
        event.getDrops().removeIf(drop -> drop.getItem().is(ModItems.IVONIS_BOOK.get()));
        ListTag list = new ListTag();
        for (ItemStack stack : keep)
            list.add(stack.saveOptional(player.registryAccess()));
        player.getPersistentData().put(KEY, list);
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer)
            return;
        ListTag list = event.getOriginal().getPersistentData().getList(KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(player.registryAccess(), list.getCompound(i));
            if (!stack.isEmpty() && !player.getInventory().add(stack))
                player.drop(stack, false);
        }
        event.getOriginal().getPersistentData().remove(KEY);
    }

    private IvonisSoulboundEvent() {
    }
}
