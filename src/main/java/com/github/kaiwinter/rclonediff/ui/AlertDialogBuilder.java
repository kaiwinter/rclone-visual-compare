package com.github.kaiwinter.rclonediff.ui;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Utility class to build {@link Alert} dialogs for specific use cases.
 */
public class AlertDialogBuilder {

  private AlertDialogBuilder() {
    // Utility Class
  }

  /**
   * Creates an {@link Alert} dialog which contains logging output. This output is taken from the
   * passed {@link List}.
   *
   * @param log
   *          the logging output
   * @return an {@link Alert} instance which may be shown or further customized
   */
  public static Alert buildLogDialog(List<String> log) {
    String logText = log.stream().collect(Collectors.joining("\r\n"));
    return buildDetailsDialog(logText);
  }

  /**
   * Creates an {@link Alert} dialog which contains an exception.
   *
   * @param throwable
   *          the exception to show
   * @return an {@link Alert} instance which may be shown or further customized
   */
  public static Alert buildExceptionDialog(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    String exceptionText = sw.toString();

    Alert alert = buildDetailsDialog(exceptionText);
    alert.setTitle("Error running rclone command");
    alert.setHeaderText("Caught an exception: " + throwable.getMessage());
    alert.setContentText("See details for full stacktrace");
    return alert;
  }

  private static Alert buildDetailsDialog(String details) {
    Alert alert = new Alert(AlertType.ERROR);

    TextArea textArea = new TextArea(details);
    textArea.setEditable(false);
    textArea.setWrapText(true);
    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane content = new GridPane();
    content.setMaxWidth(Double.MAX_VALUE);
    content.add(textArea, 0, 1);

    alert.getDialogPane().setExpandableContent(content);

    return alert;
  }
}
