package com.github.kaiwinter.rclonediff.command;

import java.io.IOException;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public abstract class AbstractCommand extends Service<Void> {

  @Override
  protected Task<Void> createTask() {
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        execute();
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
