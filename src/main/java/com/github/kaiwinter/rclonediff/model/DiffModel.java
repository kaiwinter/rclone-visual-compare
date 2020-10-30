package com.github.kaiwinter.rclonediff.model;

import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.core.DiffController;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import lombok.Data;

/**
 * Model for the {@link DiffController}.
 */
@Data
public class DiffModel {
  private StringProperty rcloneBinaryPath = new SimpleStringProperty("rclone");

  private ObjectProperty<SyncEndpoint> source = new SimpleObjectProperty<>();
  private ObjectProperty<SyncEndpoint> target = new SimpleObjectProperty<>();

  /** Files which are only on the source side only. */
  private ObservableList<SyncFile> sourceOnly = FXCollections.observableArrayList();

  /** Files which are different on source and target side. */
  private ObservableList<SyncFile> contentDifferent = FXCollections.observableArrayList();

  /** Files which are on the target side only. */
  private ObservableList<SyncFile> targetOnly = FXCollections.observableArrayList();

  private Service<Void> runningCheckCommand;

  private CopyCommand latestCopyCommand;

  /** If false, the user gets asked before any delete operation. */
  private boolean alwaysDelete = false;
}
