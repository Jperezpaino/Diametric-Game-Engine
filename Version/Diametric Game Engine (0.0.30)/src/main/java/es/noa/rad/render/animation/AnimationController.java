package es.noa.rad.render.animation;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import es.noa.rad.map.Direction8;

/**
 * Per-entity animation player. Picks the right {@link Animation} from a
 * {@code (state, direction)} table, ticks elapsed time and exposes the
 * current frame.
 *
 * <p>The elapsed clock resets whenever the state changes so the new
 * animation starts cleanly from its first frame.</p>
 *
 * @since Phase 5b
 */
public final class AnimationController {

    private final Map<AnimationState, EnumMap<Direction8, Animation>> table =
            new EnumMap<>(AnimationState.class);

    private AnimationState state = AnimationState.IDLE;
    private double elapsed;

    /**
     * Registers the animation that plays for a given {@code (state, dir)}.
     * Replaces any previous binding.
     */
    public void bind(final AnimationState animState, final Direction8 dir,
                     final Animation animation) {
        table.computeIfAbsent(animState, k -> new EnumMap<>(Direction8.class))
             .put(dir, animation);
    }

    public AnimationState getState() { return state; }

    /** Switches to {@code newState}; resets the elapsed clock if it changed. */
    public void setState(final AnimationState newState) {
        if (newState == null || newState == state) return;
        this.state   = newState;
        this.elapsed = 0;
    }

    /** Adds {@code deltaTime} seconds to the playback clock. */
    public void update(final double deltaTime) {
        if (deltaTime > 0) elapsed += deltaTime;
    }

    /**
     * @param facing direction the entity is facing
     * @return current frame image, or {@code null} if no animation is bound
     *         for the active {@code (state, facing)} pair
     */
    public BufferedImage currentFrame(final Direction8 facing) {
        final EnumMap<Direction8, Animation> perDir = table.get(state);
        if (perDir == null) return null;
        final Animation anim = perDir.get(facing);
        return anim == null ? null : anim.frameAt(elapsed);
    }
}
