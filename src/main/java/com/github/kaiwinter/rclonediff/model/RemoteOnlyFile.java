package com.github.kaiwinter.rclonediff.model;

import lombok.Data;

@Data
public class RemoteOnlyFile implements SyncFile {
  private final String file;
  private final String localPath;
  private final String remotePath;
}