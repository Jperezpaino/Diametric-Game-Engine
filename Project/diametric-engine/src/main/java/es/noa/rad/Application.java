package es.noa.rad;

import es.noa.rad.core.GameEngine;

/**
 * Application entry point. Bootstraps the {@link GameEngine}.
 */
public final class Application {

  private Application() { }

  /**
   * @param _arguments command-line arguments (unused in Phase 1)
   */
  public static void main(final String... _arguments) {
    new GameEngine().start();
  }

}
