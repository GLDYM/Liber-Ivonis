package com.liberivonis.integration;

/** Runtime target referenced by one entry node in the JSON Hub layout. */
public record HubEntry(String id, String titleKey, String titleFallback, String descriptionKey,
        String descriptionFallback, Runnable action, boolean available, boolean requiresWorld) {
}
