package es.noa.rad.entity;

import java.awt.event.KeyEvent;

import es.noa.rad.config.GameConfig;
import es.noa.rad.input.InputState;
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
 * @since Phase 1 (movement Phase 2, voxel rewrite + rules extraction Phase 3)
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

        // 1. Screen-space direction from keyboard.
        float sx = 0f, sy = 0f;
        if (input.isDown(KeyEvent.VK_W) || input.isDown(KeyEvent.VK_UP))    sy -= 1f;
        if (input.isDown(KeyEvent.VK_S) || input.isDown(KeyEvent.VK_DOWN))  sy += 1f;
        if (input.isDown(KeyEvent.VK_D) || input.isDown(KeyEvent.VK_RIGHT)) sx += 1f;
        if (input.isDown(KeyEvent.VK_A) || input.isDown(KeyEvent.VK_LEFT))  sx -= 1f;
        if (sx == 0f && sy == 0f) return;

        final boolean diagonal = sx != 0f && sy != 0f;
        final float invLen = 1f / (float) Math.hypot(sx, sy);
        sx *= invLen;
        sy *= invLen;

        // 2. Speed factor: material under us + diagonal/climb penalties.
        final WorldPoint pos = getPosition();
        final int currentCol = Math.round(pos.getCol());
        final int currentRow = Math.round(pos.getRow());
        final Tile under = validator.standingTile(currentCol, currentRow);
        // Climb is detected after the move (compare elevations); use diagonal flag now.
        final float speedFactor = TerrainEffects.combinedSpeedFactor(under, diagonal, false);

        // 3. Convert screen-space delta into world (col,row) deltas.
        final float halfW = GameConfig.TILE_WIDTH  / 2f;
        final float halfH = GameConfig.TILE_HEIGHT / 2f;
        final float pixels = (float) (getSpeed() * speedFactor * deltaTime) * GameConfig.TILE_HEIGHT;
        final float pdx = sx * pixels;
        final float pdy = sy * pixels;
        final float dCol =  pdx / (2f * halfW) + pdy / (2f * halfH);
        final float dRow = -pdx / (2f * halfW) + pdy / (2f * halfH);

        // 4. Per-axis sliding collision via MovementValidator.
        float col = pos.getCol();
        float row = pos.getRow();
        if (dCol != 0f && cellChange(col, col + dCol)
                && validator.canMove(Math.round(col), Math.round(row),
                                     Math.round(col + dCol), Math.round(row))) {
            col += dCol;
        } else if (dCol != 0f && !cellChange(col, col + dCol)) {
            col += dCol;
        }
        if (dRow != 0f && cellChange(row, row + dRow)
                && validator.canMove(Math.round(col), Math.round(row),
                                     Math.round(col), Math.round(row + dRow))) {
            row += dRow;
        } else if (dRow != 0f && !cellChange(row, row + dRow)) {
            row += dRow;
        }
        pos.setCol(col);
        pos.setRow(row);

        // 5. Snap z to the surface we are now standing on.
        pos.setZ(elevationOf(col, row));
    }

    private static boolean cellChange(final float a, final float b) {
        return Math.round(a) != Math.round(b);
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