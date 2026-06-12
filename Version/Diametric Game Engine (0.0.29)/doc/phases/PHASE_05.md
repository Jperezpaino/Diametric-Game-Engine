# Phase 5 — Animations & sprites

Frame-based animation pipeline that renders entities as directional sprites
driven by `(AnimationState, Direction8)`. Until artwork is available a
programmatic placeholder sheet exercises the full stack so logic, timing and
direction picking can be validated end-to-end.

## 5a — `SpriteSheet` + `Animation`

`SpriteSheet`
- Immutable wrapper around a `BufferedImage` and a fixed frame size.
- `frame(col, row)` returns a `BufferedImage` sub-view (no pixel copy).
- `cols` / `rows` derived from `image.getWidth() / frameWidth` and
  `image.getHeight() / frameHeight`.

`Animation`
- Holds a sheet reference, a row index, an `int[]` of column indices in
  playback order and `frameDuration` seconds.
- `frameAt(elapsedSeconds)` wraps elapsed by `frameDuration * frameCount`
  and returns the active frame; single-frame animations are valid and stay
  on that frame indefinitely.

## 5b — `AnimationState` + `AnimationController`

`AnimationState` — enum with `IDLE` and `WALK`. Designed so new states
(ATTACK, HURT, …) can be added without touching the rest of the pipeline.

`AnimationController`
- Table: `EnumMap<AnimationState, EnumMap<Direction8, Animation>>`.
- `bind(state, dir, animation)` populates the table.
- `setState(newState)` resets the elapsed clock when the state actually
  changes, so a new animation always starts on its first frame.
- `update(dt)` accumulates `elapsed`.
- `currentFrame(Direction8 facing)` returns the active frame from the
  bound `(state, facing)` animation, or `null` if no binding exists for
  that pair.

## 5c — `PlaceholderSprite` + `EntityRenderer`

`PlaceholderSprite`
- Generates a 96×384 atlas (3 cols × 8 rows): 32×48 frames, one row per
  `Direction8` value, three columns: idle, walk-bob-up, walk-bob-down.
- Each frame draws a coloured torso + head (hue derived from row index), a
  drop shadow at the feet and a white arrow pointing in the screen-space
  heading of that direction (`sx = dCol − dRow`, `sy = dCol + dRow`).
- `createController()` returns an `AnimationController` bound to every
  `(state, direction)` pair using the generated sheet.

`EntityRenderer`
- If the entity exposes an `AnimationController` and it returns a non-null
  frame for the current facing, the frame is blitted anchored by its
  bottom-centre on the entity's world position (offset `+MARKER_HALF_HEIGHT`
  so the sprite's feet sit on the diamond plane).
- Otherwise the legacy red diamond marker is drawn as a fallback.

## 5d — `Entity` field + `GameEngine` wiring

`Entity`
- New nullable `AnimationController` field + getter/setter.
- Logic classes stay unaware of animations; only the renderer reads it.

`GameEngine`
- `start()` calls `player.setAnimationController(PlaceholderSprite.createController())`
  right after `player.init(...)`.
- `update(dt)` adds an `updateAnimation(dt)` step that runs *after* movement
  and decides the active state from authoritative inputs:
  - `walking = follower.isActive() || any WASD key held`
  - `controller.setState(walking ? WALK : IDLE)`
  - `controller.update(dt)`
- This keeps `Player` and `PathFollower` ignorant of animations while still
  reflecting both keyboard and click-to-move motion correctly.

## Not in scope
- Real sprite assets (PNG atlas). The placeholder is intentional; swapping
  in real art is just a new `SpriteSheet` instance and rebinding the
  controller — no pipeline changes needed.
- Per-frame interpolation between frames (only frame-stepping).
- Animation events / callbacks (frame-tagged hooks for SFX, hitboxes…).
- Non-player entity animation (only Player has a controller bound today;
  the field is on `Entity` so NPCs can opt in without changes).
