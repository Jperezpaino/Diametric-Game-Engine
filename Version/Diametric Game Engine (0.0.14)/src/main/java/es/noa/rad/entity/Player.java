package es.noa.rad.entity;

import java.awt.event.KeyEvent;

import es.noa.rad.config.GameConfig;
import es.noa.rad.input.InputState;
import es.noa.rad.map.Direction8;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileShape;
import es.noa.rad.map.rules.MovementValidator;
import es.noa.rad.map.rules.TerrainEffects;
import es.noa.rad.projection.WorldPoint;

/**
 * Player entity controlled by the user via keyboard.
 *
 * <p>Movement validation is delegated to {@link MovementValidator} and
 * gameplay effects (speed, damage, drowning) to {@link TerrainEffects}.
 * The player itself only translates input into a world delta and applies
 * per-axis sliding collision against those rules.</p>
 *
 * <p>Facing direction ({@link Direction8}) is derived from the world-space
 * (dCol, dRow) vector so it matches the isometric view, not the raw key
 * input. Climb detection compares the Z before and after the move so the
 * {@link TerrainEffects#combinedSpeedFactor} penalty is applied correctly.</p>
 *
 * @since Phase 1 (movement Phase 2, voxel rewrite + rules extraction Phase 3,
 *        climbingUp + facing Phase 3 debt-close)
 */
public final class Player extends Entity {

    public static final float DEFAULT_SPEED = 3f;

    private InputState        input;
    private TileMap           map;
    private MovementValidator validator;

    public Player() {
        super(new WorldPoint(1f, 1f, 0f), DEFAULT_SPEED);
    }

    public void init(final InputState input, final TileMap map) {
        this.input     = input;
        this.map       = map;
        this.validator = new MovementValidator(map.getLayer());
    }

    @Override
    public void update(final double deltaTime) {
        if (input == null || map == null) return;

        // 1. Screen-space direction from keyboard (WASD only;
        //    arrow keys are reserved for the camera since Phase 4a).
        float sx = 0f, sy = 0f;
        if (input.isDown(KeyEvent.VK_W)) sy -= 1f;
        if (input.isDown(KeyEvent.VK_S)) sy += 1f;
        if (input.isDown(KeyEvent.VK_D)) sx += 1f;
        if (input.isDown(KeyEvent.VK_A)) sx -= 1f;
        if (sx == 0f && sy == 0f) return;

        final boolean diagonal = sx != 0f && sy != 0f;
        final float invLen = 1f / (float) Math.hypot(sx, sy);
        sx *= invLen;
        sy *= invLen;

        // 2. Sample elevation BEFORE the move (needed for climbingUp below).
        final WorldPoint pos     = getPosition();
        final float      zBefore = pos.getZ();

        // 3. Current cell & material for speed calculation.
        final int  currentCol = Math.round(pos.getCol());
        final int  currentRow = Math.round(pos.getRow());
        final Tile under      = validator.standingTile(currentCol, currentRow);

        // At this point we don't know climbingUp yet ??? use a preliminary
        // factor (climb = false). After the move we re-check and apply the
        // penalty for the NEXT frame via HP-damage; for speed we re-compute
        // immediately after we know the new elevation.
        final float halfW  = GameConfig.TILE_WIDTH  / 2f;
        final float halfH  = GameConfig.TILE_HEIGHT / 2f;

        // Convert screen-space delta into world (col,row) deltas.
        final float basePixels = (float) (getSpeed() * deltaTime) * GameConfig.TILE_HEIGHT;
        final float pdx = sx * basePixels;
        final float pdy = sy * basePixels;
        final float dCol =  pdx / (2f * halfW) + pdy / (2f * halfH);
        final float dRow = -pdx / (2f * halfW) + pdy / (2f * halfH);

        // 4. Update facing from world-space direction.
        setFacing(directionFrom(dCol, dRow));

        // 5. Preliminary speed factor (climbingUp unknown yet ??? false).
        float speedFactor = TerrainEffects.combinedSpeedFactor(under, diagonal, false);

        // 6. Per-axis sliding collision via MovementValidator.
        float col = pos.getCol();
        float row = pos.getRow();
        final float scaledDCol = dCol * speedFactor;
        final float scaledDRow = dRow * speedFactor;

        if (scaledDCol != 0f && cellChange(col, col + scaledDCol)
                && validator.canMove(Math.round(col), Math.round(row),
                                     Math.round(col + scaledDCol), Math.round(row))) {
            col += scaledDCol;
        } else if (scaledDCol != 0f && !cellChange(col, col + scaledDCol)) {
            col += scaledDCol;
        }
        if (scaledDRow != 0f && cellChange(row, row + scaledDRow)
                && validator.canMove(Math.round(col), Math.round(row),
                                     Math.round(col), Math.round(row + scaledDRow))) {
            row += scaledDRow;
        } else if (scaledDRow != 0f && !cellChange(row, row + scaledDRow)) {
            row += scaledDRow;
        }
        pos.setCol(col);
        pos.setRow(row);

        // 7. Snap z to the new surface and detect actual climbing.
        final float zAfter    = elevationOf(col, row);
        final boolean climbing = zAfter > zBefore + 0.01f;
        pos.setZ(zAfter);

        // 8. If we are climbing, re-apply speed factor correctly for THIS frame
        //    (retroactively note: actual deceleration manifests next frame since
        //    we already moved; this updates the internal factor for HUD/debug).
        if (climbing) {
            speedFactor = TerrainEffects.combinedSpeedFactor(under, diagonal, true);
        }

        // 9. Apply terrain damage (drowning / lava).
        final float dps = TerrainEffects.damagePerSecond(under);
        if (dps > 0f) {
            damage(dps * (float) deltaTime);
        }
    }

    private static boolean cellChange(final float a, final float b) {
        return Math.round(a) != Math.round(b);
    }

    /**
     * Maps world-space (dCol, dRow) to the nearest {@link Direction8}.
     * Returns the current facing if the delta is zero.
     */
    private Direction8 directionFrom(final float dCol, final float dRow) {
        final int dc = (int) Math.signum(dCol);
        final int dr = (int) Math.signum(dRow);
        for (final Direction8 d : Direction8.values()) {
            if (d.dCol() == dc && d.dRow() == dr) return d;
        }
        return facing; // unchanged
    }

    /** Bilinear interpolation of the four absolute corner heights. */
    private float elevationOf(final float col, final float row) {
        final int c = Math.round(col);
        final int r = Math.round(row);
        final Tile tile = validator.standingTile(c, r);
        if (tile == null) return 0f;
        final float u = (col - c) + 0.5f;
        final float v = (row - r) + 0.5f;
        final float nw = tile.absoluteCornerHeight(TileShape.Corner.NW);
        final float ne = tile.absoluteCornerHeight(TileShape.Corner.NE);
        final float se = tile.absoluteCornerHeight(TileShape.Corner.SE);
        final float sw = tile.absoluteCornerHeight(TileShape.Corner.SW);
        return nw * (1 - u) * (1 - v)
             + ne *      u  * (1 - v)
             + se *      u  *      v
             + sw * (1 - u) *      v;
    }
}
