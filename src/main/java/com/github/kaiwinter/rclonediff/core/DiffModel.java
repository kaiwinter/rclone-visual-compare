package com.github.kaiwinter.rclonediff.core;

import com.github.kaiwinter.rclonediff.command.CheckCommand;
import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;

/**
 * Model for the {@link DiffController}.
 */
@Data
public class DiffModel {
  private SyncEndpoint source;
  private SyncEndpoint target;

  /** Files which are only on the source side only. */
  private ObservableList<SyncFile> sourceOnly = FXCollections.observableArrayList();

  /** Files which are different on source and target side. */
  private ObservableList<SyncFile> contentDifferent = FXCollections.observableArrayList();

  /** Files which are on the target side only. */
  private ObservableList<SyncFile> targetOnly = FXCollections.observableArrayList();

  private CheckCommand runningCheckCommand;

  private CopyCommand latestCopyCommand;
}
