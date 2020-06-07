package com.github.kaiwinter.rclonediff.model;

import java.util.List;

import lombok.Data;

@Data
public class DirectoryEntries {
  List<DirectoryEntry> entries;
}
