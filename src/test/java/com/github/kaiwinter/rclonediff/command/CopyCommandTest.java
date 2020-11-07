package com.github.kaiwinter.rclonediff.command;

import static com.github.kaiwinter.rclonediff.util.TestFactories.SyncEndpointFactory.createLocalEndpoint;
import static com.github.kaiwinter.rclonediff.util.TestFactories.SyncEndpointFactory.createRemoteEndpoint;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
    SyncFile syncFile = new SyncFile(createRemoteEndpoint("Dropbox:/backup"), createLocalEndpoint("c:/systemp"), "file.jpg");

    CopyCommand copyCommand = new CopyCommand(syncFile);
    assertEquals("copy \"Dropbox:/backup/file.jpg\" \"c:/systemp/\"", copyCommand.getCommandline());
  }

  @Test
  void expectedReturnCode() {
    int[] actualReturnCodes = new CopyCommand(null).getExpectedReturnCodes();
    assertArrayEquals(new int[] {0}, actualReturnCodes);
  }
}
