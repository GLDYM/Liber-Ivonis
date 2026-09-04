package com.liberivonis.compat;

import com.liberivonis.client.screen.HandbookEntry;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.client.gui.book.BookAddress;
import com.klikli_dev.modonomicon.data.BookDataManager;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public final class ModonomiconCompat {
    private ModonomiconCompat() {
    }

    public static List<HandbookEntry> entries() {
        List<HandbookEntry> result = new ArrayList<>();
        for (Book book : BookDataManager.get().getBooks().values()) {
            Component title = book.getName() == null ? Component.literal(book.getId().toString())
                    : Component.translatable(book.getName());
            result.add(new HandbookEntry(title, () -> BookGuiManager.get().openBook(BookAddress.defaultFor(book))));
        }
        return result;
    }
}
