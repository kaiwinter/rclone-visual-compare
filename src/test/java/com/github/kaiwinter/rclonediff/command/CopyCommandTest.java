package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.kaiwinter.rclonediff.model.SyncFile;

/**
 * Tests for {@link CopyCommand}.
 */
class CopyCommandTest {

  /**
   * Tests the generated rclone command.
   */
  @Test
  void valid_command() {
    SyncFile syncFile = new SyncFile("Dropbox:/backup", "c:/systemp", "file.jpg");

    CopyCommand copyCommand = new CopyCommand(syncFile);
    assertEquals("copy \"Dropbox:/backup/file.jpg\" \"c:/systemp/\"", copyCommand.getCommandline());
  }
}
