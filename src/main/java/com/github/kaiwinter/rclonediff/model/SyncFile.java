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

}
