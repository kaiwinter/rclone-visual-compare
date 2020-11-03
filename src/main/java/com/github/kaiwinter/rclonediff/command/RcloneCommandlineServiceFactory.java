package com.github.kaiwinter.rclonediff.command;

import com.github.kaiwinter.rclonediff.core.PreferencesStore;

import javafx.concurrent.Service;
import lombok.RequiredArgsConstructor;

/**
 * Creates runnable {@link Service}s out of an {@link AbstractCommand}.
 */
@RequiredArgsConstructor
public class RcloneCommandlineServiceFactory {

  private final Runtime runtime;

  /**
   * Creates a runnable {@link Service} for a command.
   *
   * @param command
   *          the command
   * @return a {@link RcloneCommandlineService}
   */
  public RcloneCommandlineService createService(AbstractCommand command) {
    String loadRcloneBinaryPath = PreferencesStore.loadRcloneBinaryPath();

    RcloneCommandlineService service = new RcloneCommandlineService(runtime, loadRcloneBinaryPath, command);
    return service;
  }

  /**
   * Convenience method which creates a service (see {@link #createService(AbstractCommand)}) and
   * starts it afterwards.
   *
   * @param command
   *          the command
   */
  public void createServiceAndStart(AbstractCommand command) {
    createService(command).start();
  }
}
