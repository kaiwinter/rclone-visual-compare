package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * A file which exists local only but not on the remote path.
 */
@Data
public class LocalOnlyFile implements SyncFile {
  private final String file;
  private final String localPath;
  private final String remotePath;

  @Override
  public String toUiString() {
    return file + ": " + localPath + " -> " + remotePath;
  }
}