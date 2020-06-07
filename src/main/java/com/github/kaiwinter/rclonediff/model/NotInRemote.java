package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

@Data
public class NotInRemote {
  private final String file;
  private final String remotePath;
}