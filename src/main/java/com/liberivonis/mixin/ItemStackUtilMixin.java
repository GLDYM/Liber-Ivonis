package com.liberivonis.mixin;

import com.liberivonis.registry.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.util.ItemStackUtil;
import net.minecraft.world.item.ItemStack;

/** Makes the hub book act as a Patchouli book for quick-lookup detection. */
@Mixin(value = ItemStackUtil.class, remap = false)
public abstract class ItemStackUtilMixin {
    @Inject(method = "getBookFromStack", at = @At("RETURN"), cancellable = true)
    private static void liberIvonis$hubBook(ItemStack stack, CallbackInfoReturnable<Book> cir) {
        if (cir.getReturnValue() != null || !stack.is(ModItems.IVONIS_BOOK.get())) return;
        // The hub represents every Patchouli handbook; select the first book
        // containing the hovered stack, matching Patchouli's normal lookup.
        for (Book book : BookRegistry.INSTANCE.books.values()) {
            if (book.getContents().getEntryForStack(stack) != null) {
                cir.setReturnValue(book);
                return;
            }
        }
    }
}
