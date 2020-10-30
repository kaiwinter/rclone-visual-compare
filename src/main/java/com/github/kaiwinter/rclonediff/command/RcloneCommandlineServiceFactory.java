package com.github.kaiwinter.rclonediff.command;

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
   * @param rcloneBinaryPath
   *          the path to the rclone executable
   *
   * @param command
   *          the command
   * @return a {@link RcloneCommandlineService}
   */
  public RcloneCommandlineService createService(String rcloneBinaryPath, AbstractCommand command) {
    RcloneCommandlineService service = new RcloneCommandlineService(runtime, rcloneBinaryPath, command);
    return service;
  }

  /**
   * Convenience method which creates a service (see {@link #createService(AbstractCommand)}) and
   * starts it afterwards.
   *
   * @param rcloneBinaryPath
   *          the path to the rclone executable
   *
   * @param command
   *          the command
   */
  public void createServiceAndStart(String rcloneBinaryPath, AbstractCommand command) {
    createService(rcloneBinaryPath, command).start();
  }
}
