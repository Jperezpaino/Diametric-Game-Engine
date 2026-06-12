package es.noa.rad.render.animation;

/**
 * High-level animation states an entity can be in.
 *
 * <p>Phase 5 only needs two states. Additional ones (ATTACK, HURT, DEATH,
 * etc.) can be added without touching the rest of the animation pipeline.</p>
 *
 * @since Phase 5b
 */
public enum AnimationState {
    IDLE,
    WALK
}
