package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

/**
 * Describes an endpoint for a rclone operation.
 */
@Data
public class SyncEndpoint {

  /**
   * Whether it is a local or a remote endpoint.
   */
  public enum Type {
    /** A local path. */
    LOCAL,
    /** A rclone remote. */
    REMOTE;
  }

  private final Type type;
  private final String path;

  /**
   * Constructs a new {@link SyncEndpoint} with a normalized path (ending with an "/").
   *
   * @param type
   *          the {@link Type} of the endpoint
   * @param path
   *          the path
   */
  public SyncEndpoint(Type type, String path) {
    if (!path.endsWith("/")) {
      path = path + "/";
    }
    this.type = type;
    this.path = path;
  }

  /**
   * @return a UI representation of this {@link SyncEndpoint}
   */
  public String toUiString() {
    return path + " (" + type + ")";
  }
}
