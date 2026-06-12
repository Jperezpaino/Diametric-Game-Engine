# Diametric 2:1 Projection

The engine uses a **diametric** projection where one tile occupies `TILE_WIDTH × TILE_HEIGHT = 64 × 32` pixels — a 2:1 ratio between horizontal and vertical extent. This is the same pixel ratio popularised by classic isometric games (although mathematically it is "diametric", not true isometric).

## Constants

```
TILE_WIDTH      = 64
TILE_HEIGHT     = 32
halfTileWidth   = 32
halfTileHeight  = 16
```

## World → screen

Given world coordinates `(col, row, z)` (floats — entities can be between tiles), the screen position of the **tile centre** is:

$$
\begin{aligned}
screenX &= (col - row) \cdot halfTileWidth \\
screenY &= (col + row) \cdot halfTileHeight - z \cdot TILE\_HEIGHT
\end{aligned}
$$

## Screen → world (z = 0 plane)

To convert a pixel back into the ground plane (assuming `z = 0`):

$$
\begin{aligned}
col &= \frac{screenX}{2 \cdot halfTileWidth} + \frac{screenY}{2 \cdot halfTileHeight} \\
row &= \frac{screenY}{2 \cdot halfTileHeight} - \frac{screenX}{2 \cdot halfTileWidth}
\end{aligned}
$$

## Worked example

For the player position `col = 3, row = 3, z = 0`:

```
screenX = (3 - 3) * 32 = 0
screenY = (3 + 3) * 16 - 0 * 32 = 96
```

So tile (3, 3) is rendered with its centre at pixel `(0, 96)` in **world space** (before the camera translation).

## Painter's algorithm

Tiles are drawn with the outer loop iterating over `row` and the inner loop over `col`. Because `screenY` grows monotonically with `(col + row)`, this order guarantees that nearer tiles overdraw farther ones — no z-buffer required.
