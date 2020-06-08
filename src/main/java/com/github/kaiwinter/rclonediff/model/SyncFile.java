package com.github.kaiwinter.rclonediff.model;

/**
 * A file which needs a sync.
 */
public interface SyncFile {

  /**
   * @return the file name, may contains path fragment
   */
  String getFile();

  /**
   * @return the local path
   */
  String getLocalPath();

  /**
   * @return the remote path
   */
  String getRemotePath();

  /**
   * @return A String representation to be shown in the UI.
   */
  String toUiString();
}
