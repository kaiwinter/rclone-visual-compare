package com.github.kaiwinter.rclonediff.core;

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
  private SyncEndpoint source = new SyncEndpoint(SyncEndpoint.Type.LOCAL, "z:/2017/");
  private SyncEndpoint target = new SyncEndpoint(SyncEndpoint.Type.REMOTE, "DropboxTineCrypt:/2017/");

  /** Files which are only on the source side only. */
  private ObservableList<SyncFile> sourceOnly = FXCollections.observableArrayList();

  /** Files which are different on source and target side. */
  private ObservableList<SyncFile> contentDifferent = FXCollections.observableArrayList();

  /** Files which are on the target side only. */
  private ObservableList<SyncFile> targetOnly = FXCollections.observableArrayList();
  
  private CheckCommand runningCheckCommand;
}
