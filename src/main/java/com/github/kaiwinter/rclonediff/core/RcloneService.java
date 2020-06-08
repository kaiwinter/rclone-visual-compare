package com.github.kaiwinter.rclonediff.core;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RcloneService extends Service<Void> {
  private Runnable runnable;

  @Override
  protected Task<Void> createTask() {

    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        if (runnable != null) {
          runnable.run();
        }
        return null;
      }
    };
  }

  public void setRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

}