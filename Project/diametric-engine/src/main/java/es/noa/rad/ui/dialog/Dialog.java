package es.noa.rad.ui.dialog;

import java.util.List;

/**
 * Immutable dialog payload: a speaker name plus an ordered list of text
 * pages. Pages are rendered one at a time and advanced by the user.
 *
 * @since Phase 6d
 */
public record Dialog(String speaker, List<String> pages) {

    public Dialog {
        if (pages == null || pages.isEmpty()) {
            throw new IllegalArgumentException("Dialog requires at least one page");
        }
        pages = List.copyOf(pages);
    }

    /** Convenience factory: variadic page strings. */
    public static Dialog of(final String speaker, final String... pages) {
        return new Dialog(speaker, List.of(pages));
    }
}
