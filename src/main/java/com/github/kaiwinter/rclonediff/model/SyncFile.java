package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * A file which needs a sync.
 */
@Data
public class SyncFile {

  /**
   * the source path as selected in the UI.
   */
  private final String sourcePath;

  /**
   * the target path as selected in the UI.
   */
  private final String targetPath;

  /**
   * the file name, may contain path fragment
   */
  private final String file;

  public SyncFile(String sourcePath, String targetPath, String file) {
    if (!sourcePath.endsWith("/")) {
      sourcePath = sourcePath + "/";
    }
    if (!targetPath.endsWith("/")) {
      targetPath = targetPath + "/";
    }
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
    this.file = file;
  }
}
