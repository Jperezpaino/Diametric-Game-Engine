package es.noa.rad.audio;

import javax.sound.sampled.AudioFormat;

/**
 * Programmatic placeholder generator for {@link SoundId} clips.
 *
 * <p>Produces 16-bit signed little-endian mono PCM at 44100 Hz with a short
 * Hann-style envelope to avoid clicks. The output is meant for development
 * only: real assets dropped into {@code src/main/resources/audio/} take
 * precedence at load time (see {@link SoundManager}).</p>
 *
 * @since Phase 6a
 */
public final class PlaceholderAudio {

    public static final int   SAMPLE_RATE = 44100;
    public static final int   BITS        = 16;
    public static final int   CHANNELS    = 1;
    public static final AudioFormat FORMAT =
        new AudioFormat(SAMPLE_RATE, BITS, CHANNELS, true, false);

    private static final float MASTER_GAIN_SFX   = 0.45f;
    private static final float MASTER_GAIN_MUSIC = 0.18f;

    private PlaceholderAudio() {}

    /** Synthesises PCM bytes for the given sound. */
    public static byte[] synthClip(final SoundId id) {
        final int samples = Math.max(1, (int) ((id.durationMs() / 1000.0) * SAMPLE_RATE));
        final byte[] pcm  = new byte[samples * 2];
        final float gain  = id.isMusic() ? MASTER_GAIN_MUSIC : MASTER_GAIN_SFX;
        for (int i = 0; i < samples; i++) {
            final float t   = i / (float) SAMPLE_RATE;
            final float dur = id.durationMs() / 1000f;
            final float env = envelope(t, dur);
            final float s   = sample(id, t, dur) * env * gain;
            writeShort(pcm, i * 2, clip16(s));
        }
        return pcm;
    }

    private static float sample(final SoundId id, final float t, final float dur) {
        switch (id.shape()) {
            case SINE:
                return (float) Math.sin(2 * Math.PI * id.freqA() * t);
            case SWEEP: {
                final float f = id.freqA() + (id.freqB() - id.freqA()) * (t / dur);
                return (float) Math.sin(2 * Math.PI * f * t);
            }
            case TWO_TONE: {
                final float f = (t < dur * 0.5f) ? id.freqA() : id.freqB();
                return (float) Math.sin(2 * Math.PI * f * t);
            }
            case THUMP: {
                final float sine = (float) Math.sin(2 * Math.PI * id.freqA() * t);
                final float thud = (float) Math.exp(-12 * t);
                return sine * thud;
            }
            case NOISE: {
                final float noise = (float) (Math.random() * 2 - 1);
                final float tone  = (float) Math.sin(2 * Math.PI * id.freqA() * t);
                return 0.6f * noise + 0.4f * tone;
            }
            case ARPEGGIO: {
                final float[] notes = { id.freqA(),
                                        id.freqA() * 1.2599f,   // major third
                                        id.freqA() * 1.4983f,   // perfect fifth
                                        id.freqA() * 1.2599f };
                final float stepDur = dur / notes.length;
                final int   step    = Math.min(notes.length - 1, (int) (t / stepDur));
                return (float) Math.sin(2 * Math.PI * notes[step] * t);
            }
            default:
                return 0f;
        }
    }

    /** Smooth attack/decay envelope so the clip starts and ends silently. */
    private static float envelope(final float t, final float dur) {
        final float attack  = Math.min(0.01f, dur * 0.1f);
        final float release = Math.min(0.04f, dur * 0.2f);
        if (t < attack)              return t / attack;
        if (t > dur - release)       return Math.max(0f, (dur - t) / release);
        return 1f;
    }

    private static short clip16(final float s) {
        final int v = Math.round(s * 32767f);
        if (v >  32767) return  32767;
        if (v < -32768) return -32768;
        return (short) v;
    }

    private static void writeShort(final byte[] dst, final int offset, final short v) {
        dst[offset    ] = (byte) (v & 0xFF);
        dst[offset + 1] = (byte) ((v >>> 8) & 0xFF);
    }
}
