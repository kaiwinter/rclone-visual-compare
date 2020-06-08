package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * A file which exists remote only but not on the local path.
 */
@Data
public class RemoteOnlyFile implements SyncFile {
  private final String file;
  private final String localPath;
  private final String remotePath;

  @Override
  public String toUiString() {
    return file + ": " + remotePath + " -> " + localPath;
  }
}