package com.github.kaiwinter.rclonediff.command;

import java.io.IOException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * Parent class for rclone commands.
 */
@Slf4j
public abstract class AbstractCommand extends Service<Void> {

  @Override
  protected Task<Void> createTask() {
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          execute();
        } catch (Throwable e) {
          log.error("Error: ", e);
          throw e;
        }
        return null;
      }
    };
  }

  protected abstract void execute() throws IOException;

  protected static void wait(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
