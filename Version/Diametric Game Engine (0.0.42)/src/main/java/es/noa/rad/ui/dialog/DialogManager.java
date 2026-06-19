package es.noa.rad.ui.dialog;

/**
 * Mutable state for the currently open {@link Dialog}.
 *
 * <p>The manager exposes the minimal API the engine needs: open a dialog,
 * advance to the next page (closing on the last one), query whether one is
 * active and which page index to render. All mutations are single-threaded
 * (driven by the game loop), so no synchronisation is needed.</p>
 *
 * @since Phase 6d
 */
public final class DialogManager {

    private Dialog active;
    private int    page;

    public boolean isActive()   { return active != null; }
    public Dialog  active()     { return active; }
    public int     pageIndex()  { return page; }

    public String currentPage() {
        return active == null ? null : active.pages().get(page);
    }

    public boolean isLastPage() {
        return active != null && page >= active.pages().size() - 1;
    }

    public void show(final Dialog dialog) {
        this.active = dialog;
        this.page   = 0;
    }

    /** Moves to the next page or closes the dialog if we were on the last one. */
    public void advance() {
        if (active == null) return;
        if (page + 1 >= active.pages().size()) {
            close();
        } else {
            page++;
        }
    }

    public void close() {
        this.active = null;
        this.page   = 0;
    }
}
