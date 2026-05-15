package es.noa.rad.entity;

import java.awt.event.KeyEvent;

import es.noa.rad.config.GameConfig;
import es.noa.rad.input.InputState;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.WorldPoint;

/**
 * Player entity controlled by the user via keyboard.
 *
 * <h3>Screen-space (compass) movement mapping</h3>
 * <pre>
 *   W            →  North
 *   S            →  South
 *   D            →  East
 *   A            →  West
 *   W + A        →  North-West
 *   W + D        →  North-East
 *   S + A        →  South-West
 *   S + D        →  South-East
 * </pre>
 *
 * <p>The keys describe a screen-space direction; the world-space deltas
 * ({@code col}, {@code row}) are derived through the inverse of the
 * 2:1 diametric projection, so movement always feels intuitive on screen
 * regardless of the underlying tile orientation.</p>
 *
 * <p>Collision is checked per-axis: the new column or row is only applied
 * when crossing into a walkable tile, allowing slide-along-walls behaviour.</p>
 *
 * @since Phase 1 (movement added Phase 2)
 */
public final class Player extends Entity {

    /** Default speed in tiles per second (screen-space tile heights). */
    public static final float DEFAULT_SPEED = 3f;

    private InputState input;
    private TileMap    map;

    /** Creates a player at column 1, row 1, z 0 (walkable grass tile). */
    public Player() {
        super(new WorldPoint(1f, 1f, 0f), DEFAULT_SPEED);
    }

    /**
     * Injects the input and map dependencies needed for movement.
     *
     * @param input keyboard snapshot
     * @param map   tile map used for collision checks
     */
    public void init(final InputState input, final TileMap map) {
        this.input = input;
        this.map   = map;
    }

    @Override
    public void update(final double deltaTime) {
        if (input == null || map == null) {
            return;
        }

        // ---------------------------------------------------------------------
        // 1. Build the screen-space direction vector from the keyboard.
        // ---------------------------------------------------------------------
        float sx = 0f; // east(+) / west(-)
        float sy = 0f; // south(+) / north(-)

        if (input.isDown(KeyEvent.VK_W) || input.isDown(KeyEvent.VK_UP))    { sy -= 1f; }
        if (input.isDown(KeyEvent.VK_S) || input.isDown(KeyEvent.VK_DOWN))  { sy += 1f; }
        if (input.isDown(KeyEvent.VK_D) || input.isDown(KeyEvent.VK_RIGHT)) { sx += 1f; }
        if (input.isDown(KeyEvent.VK_A) || input.isDown(KeyEvent.VK_LEFT))  { sx -= 1f; }

        if (sx == 0f && sy == 0f) {
            return;
        }

        // Normalise so diagonals don't move √2× faster.
        final float invLen = 1f / (float) Math.hypot(sx, sy);
        sx *= invLen;
        sy *= invLen;

        // ---------------------------------------------------------------------
        // 2. Convert the screen-space displacement into world deltas.
        //
        // Forward projection (z=0):
        //   screenX = (col - row) * halfW
        //   screenY = (col + row) * halfH
        //
        // Inverse:
        //   col =  screenX / (2*halfW) + screenY / (2*halfH)
        //   row = -screenX / (2*halfW) + screenY / (2*halfH)
        //
        // Speed is expressed in "tile heights per second" so North/South
        // produce the same |dCol|+|dRow| magnitude as the previous phase.
        // ---------------------------------------------------------------------
        final float halfW = GameConfig.TILE_WIDTH  / 2f;
        final float halfH = GameConfig.TILE_HEIGHT / 2f;
        final float pixels = (float) (getSpeed() * deltaTime) * GameConfig.TILE_HEIGHT;

        final float pdx = sx * pixels;
        final float pdy = sy * pixels;

        final float dCol =  pdx / (2f * halfW) + pdy / (2f * halfH);
        final float dRow = -pdx / (2f * halfW) + pdy / (2f * halfH);

        // ---------------------------------------------------------------------
        // 3. Apply collision per-axis (slide along walls).
        // ---------------------------------------------------------------------
        final WorldPoint pos = getPosition();
        float col = pos.getCol();
        float row = pos.getRow();

        if (dCol != 0f && isMoveAllowed(col, row, col + dCol, row)) {
            col += dCol;
        }
        if (dRow != 0f && isMoveAllowed(col, row, col, row + dRow)) {
            row += dRow;
        }

        pos.setCol(col);
        pos.setRow(row);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when moving from {@code (fromCol,fromRow)} to
     * {@code (toCol,toRow)} is allowed. A move within the same integer tile
     * is always allowed (sub-tile motion); only crossing into a new tile
     * triggers a walkability check.
     */
    private boolean isMoveAllowed(final float fromCol, final float fromRow,
                                  final float toCol,   final float toRow) {
        final int fc = Math.round(fromCol);
        final int fr = Math.round(fromRow);
        final int tc = Math.round(toCol);
        final int tr = Math.round(toRow);
        if (fc == tc && fr == tr) {
            return true;
        }
        final var tile = map.getVisualLayer().getTile(tc, tr);
        return tile != null && tile.isWalkable();
    }
}
