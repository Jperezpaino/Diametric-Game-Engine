package es.noa.rad.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * In-memory representation of a v2 map document.
 *
 * <p>This is the pure domain model the rest of the engine consumes when
 * working with the Map System spec (clause 11). It carries:</p>
 *
 * <ul>
 *   <li>The map's spatial extent ({@code width}, {@code depth}, {@code length}
 *       &mdash; {@code length} is the Z range, i.e. number of elevation
 *       slots).</li>
 *   <li>A list of {@link TileInstance}s with <b>absolute</b> coordinates
 *       (each placed tile resolves through the
 *       {@code TileDefinitionRegistry}).</li>
 *   <li>A list of {@link StructureInstance}s that re-use catalogued
 *       {@link Structure}s; their tiles are added to the world by
 *       translating each contained {@link TileInstance} by the structure
 *       instance's origin.</li>
 * </ul>
 *
 * <p>The model is intentionally <b>I/O-free</b>: serialisation, version
 * negotiation and the legacy {@code (TileMaterial, TileShape)} palette shim
 * live in Phase 9d's {@code MapResourceIO} / updated {@code MapLoader}.
 * What 9c gives us is a stable type to reason about a map without going
 * through the runtime {@link TileMap} grid &mdash; e.g. an editor that
 * never instantiates the runtime layer can still build a valid
 * {@code MapResource} and round-trip it through Phase 9d's I/O.</p>
 *
 * @param width      Number of columns along the X axis (must be {@code > 0}).
 * @param depth      Number of rows    along the Y axis (must be {@code > 0}).
 * @param length     Number of Z slots (must be {@code > 0}).
 * @param tiles      Tiles placed directly at absolute coordinates.
 * @param structures Structure instances placed at absolute coordinates.
 *
 * @since Phase 9c
 */
public record MapResource(int width,
                          int depth,
                          int length,
                          List<TileInstance>      tiles,
                          List<StructureInstance> structures) {

    public MapResource {
        if (width  <= 0) throw new IllegalArgumentException("MapResource width must be > 0 (got "  + width  + ")");
        if (depth  <= 0) throw new IllegalArgumentException("MapResource depth must be > 0 (got "  + depth  + ")");
        if (length <= 0) throw new IllegalArgumentException("MapResource length must be > 0 (got " + length + ")");
        tiles      = (tiles      == null) ? List.of() : List.copyOf(tiles);
        structures = (structures == null) ? List.of() : List.copyOf(structures);
    }

    /**
     * Builds a {@code MapResource} from the current runtime {@link TileMap}.
     *
     * <p>Each runtime voxel is exported as a {@link TileInstance} carrying
     * its {@link Tile#definitionId() definition id} (Phase 8d-bis flipped
     * the runtime so that id is always available). Structure instances are
     * not synthesised here &mdash; runtime maps don't track which tiles
     * came from which structure, so the caller must layer that information
     * back in if they need it (e.g. by remembering authoring intent in the
     * editor).</p>
     */
    public static MapResource fromTileMap(final TileMap map) {
        Objects.requireNonNull(map, "map");
        final TileLayer layer = map.getLayer();
        final int width = layer.getWidth();
        final int depth = layer.getDepth();
        final int[] maxZ = { -1 };
        final List<TileInstance> tiles = new ArrayList<>();
        layer.forEachTile((col, row, z, tile) -> {
            tiles.add(new TileInstance(tile.definitionId(), col, row, z));
            if (z > maxZ[0]) maxZ[0] = z;
        });
        final int length = Math.max(1, maxZ[0] + 1);
        return new MapResource(width, depth, length, tiles, List.of());
    }
}
