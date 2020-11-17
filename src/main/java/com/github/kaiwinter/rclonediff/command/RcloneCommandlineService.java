package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.kaiwinter.rclonediff.util.AlertDialogBuilder;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An executable {@link Service} which runs an {@link AbstractCommand} implementation.
 */
@Slf4j
@RequiredArgsConstructor
public class RcloneCommandlineService extends Service<Void> {

  private final Runtime runtime;
  private final String rcloneBinaryPath;
  private final AbstractCommand command;

  /**
   * Return code of the rclone command.
   */
  @Getter
  protected int returnCode = -1;

  /**
   * The rclone output is stored in this list and shown on errors while running the command.
   */
  @Getter
  protected List<String> consoleLog = new ArrayList<>();

  private Process process;

  private boolean isCancelled;

  @Override
  protected Task<Void> createTask() {
    // Exceptions which are thrown from this service should be shown as an Alert dialog to the user
    this.setOnFailed(event -> {
      Throwable exception = event.getSource().getException();
      Alert alert = AlertDialogBuilder.buildExceptionDialog(exception);
      Platform.runLater(() -> alert.showAndWait());
    });
    this.setOnSucceeded(event -> {
      if (Arrays.stream(command.getExpectedReturnCodes()).anyMatch(code -> code == returnCode)) {
        command.getCommandSucceededEvent().run();
      } else {
        Alert alert = AlertDialogBuilder.buildLogDialog(consoleLog);
        alert.setTitle("Error running rclone command");
        alert.setHeaderText("Unexpected rclone return code: " + returnCode);
        alert.setContentText("See details for rclone command and rclone console output");

        Platform.runLater(() -> alert.showAndWait());
      }
      event.consume();
    });

    return new Task<>() {
      @Override
      protected Void call() throws Exception {
        executeCommand();
        return null;
      }
    };
  }

  private void executeCommand() throws IOException {
    String commandline = String.join(" ", rcloneBinaryPath, command.getCommandline());

    log.info("Command: {}", commandline);
    consoleLog.add(commandline);

    process = runtime.exec(commandline);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      if (isCancelled) {
        break;
      }

      log.info(line);
      consoleLog.add(line);
      command.handleRcloneOutput(line);
    }
    wait(process);

    returnCode = process.exitValue();
    log.info("rclone return code: {}", returnCode);
  }

  @Override
  protected void cancelled() {
    isCancelled = true;
    process.destroy();
  }

  private static void wait(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException exception) {
      Alert alert = AlertDialogBuilder.buildExceptionDialog(exception);
      Platform.runLater(() -> alert.showAndWait());
    }
  }
}
