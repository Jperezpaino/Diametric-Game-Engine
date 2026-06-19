package es.noa.rad.pathfinding;

/**
 * A single waypoint of a path: an integer cell (col, row).
 *
 * <p>Z is implicit: the actor stands on the top tile of the column at
 * {@code (col, row)}.  Voxel layers with multiple stacked floors will require
 * a {@code (col, row, z)} extension in a future phase.</p>
 *
 * @since Phase 4c
 */
public record PathNode(int col, int row) { }
