package es.noa.rad.audio;

/**
 * Catalogue of every sound effect / music track the engine can play.
 *
 * <p>Each entry declares both the classpath resource path of the real asset
 * (WAV PCM, signed 16-bit, 44100 Hz) and a fallback synthesis recipe used
 * by {@link PlaceholderAudio} when the asset file is not present. This lets
 * Phase 6a/6b run end-to-end before the artist drops the final WAV files.</p>
 *
 * @since Phase 6a
 */
public enum SoundId {

    FOOTSTEP      ("audio/sfx/footstep.wav",      Shape.THUMP,    80f,    0f,  120, false),
    CLICK         ("audio/sfx/click.wav",         Shape.SINE,    1200f,   0f,   40, false),
    CLICK_INVALID ("audio/sfx/click_invalid.wav", Shape.NOISE,    200f,   0f,   90, false),
    ARRIVAL       ("audio/sfx/arrival.wav",       Shape.TWO_TONE, 440f,  660f, 260, false),
    HURT          ("audio/sfx/hurt.wav",          Shape.SWEEP,    600f,  200f, 180, false),
    DEATH         ("audio/sfx/death.wav",         Shape.SWEEP,    400f,   80f, 700, false),
    MUSIC_THEME   ("audio/music/theme.wav",       Shape.ARPEGGIO, 261.6f, 0f, 4000, true);

    public enum Shape { SINE, SWEEP, TWO_TONE, THUMP, NOISE, ARPEGGIO }

    private final String  resourcePath;
    private final Shape   shape;
    private final float   freqA;
    private final float   freqB;
    private final int     durationMs;
    private final boolean music;

    SoundId(final String resourcePath, final Shape shape,
            final float freqA, final float freqB,
            final int durationMs, final boolean music) {
        this.resourcePath = resourcePath;
        this.shape        = shape;
        this.freqA        = freqA;
        this.freqB        = freqB;
        this.durationMs   = durationMs;
        this.music        = music;
    }

    public String  resourcePath() { return resourcePath; }
    public Shape   shape()        { return shape; }
    public float   freqA()        { return freqA; }
    public float   freqB()        { return freqB; }
    public int     durationMs()   { return durationMs; }
    public boolean isMusic()      { return music; }
}
