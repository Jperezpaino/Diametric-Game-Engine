package es.noa.rad.entity;

import java.awt.event.KeyEvent;

import es.noa.rad.config.GameConfig;
import es.noa.rad.input.InputState;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileType;
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

        // ---------------------------------------------------------------------
        // 4. Snap z to whatever tile we are now standing on.
        // ---------------------------------------------------------------------
        pos.setZ(elevationOf(col, row));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when moving from {@code (fromCol,fromRow)} to
     * {@code (toCol,toRow)} is allowed.
     *
     * <p>Two integer tiles share an edge made of two diamond corners. The
     * crossing is allowed only when the absolute heights at those two
     * corners match between source and destination tiles &mdash; i.e. the
     * two surfaces meet flush. This naturally forbids stepping from a flat
     * tile onto an elevated one and forces the player to use a ramp whose
     * low edge matches the ground and whose high edge matches the plateau.</p>
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
        final Tile to = map.getVisualLayer().getTile(tc, tr);
        if (to == null || !to.isWalkable()) {
            return false;
        }
        final Tile from = map.getVisualLayer().getTile(fc, fr);
        if (from == null) {
            return false;
        }

        // Identify the two corners shared by the source/destination edge.
        // Corner indices follow TileType.{N,E,S,W} = {0,1,2,3}.
        // For each direction the array stores 4 indices in pairs:
        //   { fromCornerA, toCornerA, fromCornerB, toCornerB }
        // and the move is allowed when the two corner pairs match in height.
        final int dc = tc - fc;
        final int dr = tr - fr;
        final int[] shared;
        if      (dc ==  1 && dr ==  0) { shared = new int[] { TileType.E, TileType.N, TileType.S, TileType.W }; }
        else if (dc == -1 && dr ==  0) { shared = new int[] { TileType.N, TileType.E, TileType.W, TileType.S }; }
        else if (dc ==  0 && dr ==  1) { shared = new int[] { TileType.S, TileType.E, TileType.W, TileType.N }; }
        else if (dc ==  0 && dr == -1) { shared = new int[] { TileType.N, TileType.W, TileType.E, TileType.S }; }
        else { return false; } // diagonal grid jumps are not supported by per-axis collision

        return cornerHeight(from, shared[0]) == cornerHeight(to,   shared[1])
            && cornerHeight(from, shared[2]) == cornerHeight(to,   shared[3]);
    }

    private static int cornerHeight(final Tile tile, final int corner) {
        return tile.getElevation() + tile.getType().getCornerHeight(corner);
    }

    /**
     * Returns the height the player adopts when standing at fractional
     * position {@code (col, row)}. The result is the bilinear interpolation
     * of the four corner heights of the underlying tile, so ramps produce
     * a smooth, gradual climb instead of an instant snap.
     */
    private float elevationOf(final float col, final float row) {
        final int c = Math.round(col);
        final int r = Math.round(row);
        final Tile tile = map.getVisualLayer().getTile(c, r);
        if (tile == null) {
            return 0f;
        }
        // Local coordinates inside the tile, in [-0.5, +0.5].
        final float lc = col - c;
        final float lr = row - r;
        // Bilinear weights: u along col, v along row, both in [0, 1].
        final float u = lc + 0.5f;
        final float v = lr + 0.5f;
        // Corner positions (u, v): N(0,0) E(1,0) S(1,1) W(0,1).
        final float n = cornerHeight(tile, TileType.N);
        final float e = cornerHeight(tile, TileType.E);
        final float s = cornerHeight(tile, TileType.S);
        final float w = cornerHeight(tile, TileType.W);
        return n * (1f - u) * (1f - v)
             + e *        u  * (1f - v)
             + s *        u  *        v
             + w * (1f - u) *        v;
    }
}
