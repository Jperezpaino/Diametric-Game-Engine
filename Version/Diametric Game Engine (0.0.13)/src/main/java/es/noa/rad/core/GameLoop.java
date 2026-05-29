package es.noa.rad.core;

import es.noa.rad.config.GameConfig;

/**
 * Main game loop running on its own thread.
 *
 * <p>Decouples logic updates (UPS) from frame rendering (FPS) using
 * {@link System#nanoTime()} accumulators. Real FPS / UPS are reported every
 * second to the standard output.</p>
 *
 * @since Phase 1
 */
public final class GameLoop implements Runnable {

    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final GameEngine engine;
    private final Thread thread;

    private volatile boolean running;
    private int currentFps;
    private int currentUps;

    public GameLoop(final GameEngine engine) {
        this.engine = engine;
        this.thread = new Thread(this, "DiametricGameLoop");
    }

    /** Starts the loop thread. */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        thread.start();
    }

    /** Requests the loop to stop. */
    public void stop() {
        running = false;
    }

    public int getCurrentFps() { return currentFps; }
    public int getCurrentUps() { return currentUps; }

    @Override
    public void run() {
        final double updateInterval = NANOS_PER_SECOND / GameConfig.TARGET_UPS;
        final double renderInterval = NANOS_PER_SECOND / GameConfig.TARGET_FPS;

        long previousTime = System.nanoTime();
        double updateAccumulator = 0;
        double renderAccumulator = 0;

        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            final long now = System.nanoTime();
            final long elapsed = now - previousTime;
            previousTime = now;

            updateAccumulator += elapsed;
            renderAccumulator += elapsed;

            // Catch up on logic updates.
            while (updateAccumulator >= updateInterval) {
                engine.update(updateInterval / NANOS_PER_SECOND);
                updateAccumulator -= updateInterval;
                updates++;
            }

            // Render one frame when its slot is due.
            if (renderAccumulator >= renderInterval) {
                engine.render();
                renderAccumulator -= renderInterval;
                frames++;
            }

            // FPS / UPS report once per second.
            if (System.currentTimeMillis() - timer >= 1000) {
                currentFps = frames;
                currentUps = updates;
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            // Light sleep to avoid 100% CPU when both buckets are not due yet.
            try {
                Thread.sleep(1);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
}
