package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import javafx.application.Platform;

/**
 * Tests for {@link DeleteCommand}.
 */
@SuppressWarnings("resource")
class DeleteCommandTest {

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

    DeleteCommand deleteCommand = new DeleteCommand(runtime, "Dropbox:/backup/file1");
    deleteCommand.createTask().run();

    verify(runtime).exec(eq("rclone delete \"Dropbox:/backup/file1\""));
  }

  /**
   * Tests if the return code was set.
   */
  @Test
  void return_code() throws IOException {
    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[] {})).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    DeleteCommand deleteCommand = new DeleteCommand(runtime, "Dropbox:/backup/file1");
    deleteCommand.createTask().run();

    assertEquals(0, deleteCommand.getReturnCode());
  }
}
