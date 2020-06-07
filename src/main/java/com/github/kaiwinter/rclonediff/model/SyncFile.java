package com.github.kaiwinter.rclonediff.model;

public interface SyncFile {
  String getFile();

  String getLocalPath();

  String getRemotePath();
}
