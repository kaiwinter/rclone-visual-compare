package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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
    DeleteCommand deleteCommand = new DeleteCommand("Dropbox:/backup/file1");
    assertArrayEquals(new String[] { "delete", "Dropbox:/backup/file1" }, deleteCommand.getCommandline());
  }

  @Test
  void expectedReturnCode() {
    int[] actualReturnCodes = new DeleteCommand(null).getExpectedReturnCodes();
    assertArrayEquals(new int[] {0}, actualReturnCodes);
  }
}
