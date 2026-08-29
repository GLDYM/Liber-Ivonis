package com.liberivonis.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class IvonisBookItem extends Item {
    public IvonisBookItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // ClientEventHandlers owns the client-only interaction hook. Keeping this
        // item class free of client GUI references makes dedicated servers safe.
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
