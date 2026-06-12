package es.noa.rad.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Centralised audio mixer for SFX and music.
 *
 * <p>At construction it pre-loads one {@link Clip} per {@link SoundId}. It
 * first tries the classpath resource declared in the enum; if that file is
 * missing or unreadable it falls back to a programmatic placeholder produced
 * by {@link PlaceholderAudio}. This guarantees the engine has audio output
 * even before the artist drops the final WAV files.</p>
 *
 * <p>Failures during initialisation are logged and silently ignored: audio
 * is non-critical for the game loop.</p>
 *
 * @since Phase 6a
 */
public final class SoundManager {

    private final Map<SoundId, Clip> clips = new EnumMap<>(SoundId.class);
    private Clip musicClip;

    public SoundManager() {
        for (final SoundId id : SoundId.values()) {
            final Clip clip = loadClip(id);
            if (clip != null) {
                clips.put(id, clip);
            }
        }
    }

    /** Triggers a one-shot SFX. Restarts the clip if it is already playing. */
    public void play(final SoundId id) {
        final Clip clip = clips.get(id);
        if (clip == null || id.isMusic()) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    /** Starts (or restarts) the looping music track. */
    public void loopMusic(final SoundId id) {
        if (!id.isMusic()) return;
        stopMusic();
        final Clip clip = clips.get(id);
        if (clip == null) return;
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip = clip;
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip = null;
        }
    }

    /** Closes every line. Call on shutdown. */
    public void close() {
        stopMusic();
        for (final Clip clip : clips.values()) {
            try { clip.close(); } catch (Exception ignored) { /* no-op */ }
        }
        clips.clear();
    }

    private static Clip loadClip(final SoundId id) {
        try {
            final AudioInputStream src = openStream(id);
            if (src == null) return null;
            final Clip clip = AudioSystem.getClip();
            clip.open(src);
            src.close();
            return clip;
        } catch (LineUnavailableException ex) {
            System.err.println("[SoundManager] no audio line available for " + id);
            return null;
        } catch (Exception ex) {
            System.err.println("[SoundManager] failed to load " + id + ": " + ex.getMessage());
            return null;
        }
    }

    private static AudioInputStream openStream(final SoundId id) throws Exception {
        final InputStream res = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(id.resourcePath());
        if (res != null) {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(res));
        }
        // Fallback: synthesise a placeholder PCM buffer.
        final byte[] pcm = PlaceholderAudio.synthClip(id);
        final ByteArrayInputStream bais = new ByteArrayInputStream(pcm);
        return new AudioInputStream(bais, PlaceholderAudio.FORMAT,
                pcm.length / PlaceholderAudio.FORMAT.getFrameSize());
    }
}
