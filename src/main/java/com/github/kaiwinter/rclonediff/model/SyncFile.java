package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * A file which needs a sync.
 */
@Data
public class SyncFile {

  /**
   * the source endpoint as selected in the UI.
   */
  private final SyncEndpoint sourceEndpoint;

  /**
   * the target Endpoint as selected in the UI.
   */
  private final SyncEndpoint targetEndpoint;

  /**
   * the file name, may contain path fragment
   */
  private final String file;
}
