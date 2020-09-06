package com.github.kaiwinter.rclonediff.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.kaiwinter.rclonediff.ui.AlertDialogBuilder;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import lombok.Getter;

/**
 * Parent class for rclone commands.
 */
public abstract class AbstractCommand extends Service<Void> {

  /**
   * Return code of the rclone command. Set to -1 so implementations have to set it properly.
   */
  @Getter
  protected int returnCode = -1;

  /**
   * Implementations can store console log into this list.
   */
  @Getter
  protected List<String> consoleLog = new ArrayList<>();

  @Override
  protected Task<Void> createTask() {
    // Exceptions which are thrown from this service should be shown as an Alert dialog to the user
    this.setOnFailed(event -> {
      Throwable exception = event.getSource().getException();
      Alert alert = AlertDialogBuilder.buildExceptionDialog(exception);
      Platform.runLater(() -> alert.showAndWait());
    });

    return new Task<>() {
      @Override
      protected Void call() throws Exception {
        execute();
        return null;
      }
    };
  }

  protected static void wait(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException exception) {
      Alert alert = AlertDialogBuilder.buildExceptionDialog(exception);
      Platform.runLater(() -> alert.showAndWait());
    }
  }

  /**
   * Executes this service (this command).
   *
   * @throws IOException
   *           if executing the rclone command fails
   */
  protected abstract void execute() throws IOException;

  /**
   * A successful rclone command returns mostly a 0 as successful return code. But some commands
   * return different codes for success.
   *
   * @return the return codes of the successful rclone run
   */
  public abstract int[] getExpectedReturnCodes();
}
