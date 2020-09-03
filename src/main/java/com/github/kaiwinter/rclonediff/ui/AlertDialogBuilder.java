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

public class AlertDialogBuilder {

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

  public static Alert buildExceptionDialog(Exception exception) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    String exceptionText = sw.toString();

    return buildDetailsDialog(exceptionText);
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
