package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
    SyncFile syncFile = new SyncFile("file.jpg");
    Path tempDirectory = Path.of("c:/systemp");

    CopyCommand copyCommand = new CopyCommand(runtime, "Dropbox:/backup", syncFile, tempDirectory);
    copyCommand.createTask().run();

    verify(runtime).exec(eq("rclone copy Dropbox:/backup/file.jpg c:" + File.separator + "systemp"));
  }

  /**
   * Tests if the image gets initialized (with an invalid file).
   */
  @Test
  void load_image() throws IOException {
    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[] {})).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    SyncFile syncFile = new SyncFile("file.jpg");
    Path tempDirectory = Path.of("c:/systemp");

    CopyCommand copyCommand = new CopyCommand(runtime, "Dropbox:/backup", syncFile, tempDirectory);
    copyCommand.createTask().run();

    assertNotNull(copyCommand.getLoadedImage());
  }

}
