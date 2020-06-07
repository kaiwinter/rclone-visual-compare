package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

@Data
public class NotInLocal {
  private final String file;
  private final String localPath;
}