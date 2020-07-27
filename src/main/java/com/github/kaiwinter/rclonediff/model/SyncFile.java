package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * A file which needs a sync.
 */
@Data
public class SyncFile {

  /**
   * the file name, may contain path fragment
   */
  private final String file;

  /**
   * the local path
   */
  private final String localPath;

  /**
   * the remote path
   */
  private final String remotePath;

  /**
   * @return a String representation to be shown in the UI.
   */
  public String toUiString() {
    return file + ": " + localPath + " -> " + remotePath;
  }

}
