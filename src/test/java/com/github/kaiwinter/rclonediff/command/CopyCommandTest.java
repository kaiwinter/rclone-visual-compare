package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;

/**
 * Tests for {@link CopyCommand}.
 */
@SuppressWarnings("resource")
class CopyCommandTest {

  @BeforeAll
  public static void initToolkit() {
    try {
      Platform.startup(() -> {
        // Initialize Toolkit
      });
    } catch (IllegalStateException e) {
      // initialized by previous test
    }
  }

  /**
   * Tests the generated rclone command.
   */
  @Test
  void valid_command() throws IOException {
    Runtime runtime = mock(Runtime.class, Answers.RETURNS_MOCKS);
    SyncFile syncFile = new SyncFile("Dropbox:/backup", "c:/systemp", "file.jpg");

    CopyCommand copyCommand = new CopyCommand(runtime, syncFile);
    copyCommand.createTask().run();

    verify(runtime).exec(eq("rclone copy \"Dropbox:/backup/file.jpg\" \"c:" + File.separator + "systemp\""));
  }

  /**
   * Tests if the return code was set.
   */
  @Test
  void return_code() throws IOException {
    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[] {})).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    SyncFile syncFile = new SyncFile("Dropbox:/backup", "c:/systemp", "file.jpg");

    CopyCommand copyCommand = new CopyCommand(runtime, syncFile);
    copyCommand.createTask().run();

    assertEquals(0, copyCommand.getReturnCode());
  }
}
