package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DeleteCommand}.
 */
class DeleteCommandTest {

  /**
   * Tests the generated rclone command.
   */
  @Test
  void valid_command() throws IOException {
    DeleteCommand deleteCommand = new DeleteCommand("rclone", "Dropbox:/backup/file1");
    assertEquals("rclone delete \"Dropbox:/backup/file1\"", deleteCommand.getCommandline());
  }
}
