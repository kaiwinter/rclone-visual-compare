package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

@Data
public class LocalOnlyFile implements SyncFile {
  private final String file;
  private final String localPath;
  private final String remotePath;
}