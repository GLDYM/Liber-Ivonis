package com.liberivonis.compat;

import com.liberivonis.client.screen.HandbookEntry;
import net.minecraft.network.chat.Component;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

import java.util.ArrayList;
import java.util.List;

public final class PatchouliCompat {
    private PatchouliCompat() {
    }

    public static List<HandbookEntry> entries() {
        List<HandbookEntry> result = new ArrayList<>();
        for (Book book : BookRegistry.INSTANCE.books.values()) {
            // Patchouli stores the book title as a translation key in `name`.
            // Using a literal here makes ModernUI display the key itself when
            // the book is localized (and is why the registration name leaked
            // into the Hub label).
            result.add(new HandbookEntry(Component.translatable(book.name),
                    () -> ClientBookRegistry.INSTANCE.displayBookGui(book.id, null, 0)));
        }
        return result;
    }
}
