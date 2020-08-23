package com.github.kaiwinter.rclonediff.model;

import javafx.beans.property.SimpleStringProperty;
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
    LOCAL, REMOTE;
  }

  private final Type type;
  private final SimpleStringProperty path;

  public SyncEndpoint(Type type, String string) {
    this.type = type;
    this.path = new SimpleStringProperty(string);
  }
}
