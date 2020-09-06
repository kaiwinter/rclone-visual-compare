package com.github.kaiwinter.rclonediff.core;

import java.util.Arrays;

import com.github.kaiwinter.rclonediff.command.AbstractCommand;
import com.github.kaiwinter.rclonediff.ui.AlertDialogBuilder;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;

/**
 * Handles the case of a succeeded command. If the command succeeded successful the passed
 * {@link Runnable} gets executed. If the command was not successful a {@link Alert} is shown which
 * contains the console log output.
 */
@RequiredArgsConstructor
public class CommandSucceededEvent implements EventHandler<WorkerStateEvent> {

  private final AbstractCommand command;
  private final Runnable successAction;

  @Override
  public void handle(WorkerStateEvent event) {
    if (Arrays.stream(command.getExpectedReturnCodes()).anyMatch(code -> code == command.getReturnCode())) {
      successAction.run();
    } else {
      Alert alert = AlertDialogBuilder.buildLogDialog(command.getConsoleLog());
      alert.setTitle("Error running rclone command");
      alert.setHeaderText("Unexpected rclone return code: " + command.getReturnCode());
      alert.setContentText("See details for rclone command and rclone console output");

      Platform.runLater(() -> alert.showAndWait());
    }
    event.consume();
  }
}
