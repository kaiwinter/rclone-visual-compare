package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

@Data
public class DirectoryEntry {
  private String path;
  private String name;
  private long size;
  private String mimeType;
  private String modTime;
  private boolean isDir;
}
