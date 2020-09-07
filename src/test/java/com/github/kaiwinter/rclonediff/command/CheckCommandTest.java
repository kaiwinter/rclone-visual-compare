package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import com.github.kaiwinter.rclonediff.core.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Tests for {@link CheckCommand}.
 */
@SuppressWarnings("resource")
class CheckCommandTest {

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

    DiffModel model = new DiffModel();
    model.setSource(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.setTarget(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(runtime, model);
    checkCommand.createTask().run();

    verify(runtime).exec(eq("rclone check c:/temp/ Dropbox:/backup"));
  }

  /**
   * Tests if files which are not on local side are parsed correctly.
   */
  @Test
  void not_in_local() throws IOException {
    String rcloneOutput = "2020/05/26 15:17:06 ERROR : 20200501_081347.mp4: File not in Local file system at //?/c:/temp/";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    DiffModel model = new DiffModel();
    model.setSource(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.setTarget(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(runtime, model);
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(model.getSourceOnly().isEmpty());
    assertTrue(model.getContentDifferent().isEmpty());
    List<SyncFile> notInLocal = model.getTargetOnly();
    assertEquals(1, notInLocal.size());

    SyncFile syncFile = notInLocal.get(0);
    assertEquals("20200501_081347.mp4", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup", syncFile.getTargetPath());
  }

  /**
   * Tests if files which are not the remote side are parsed correctly.
   */
  @Test
  void not_in_remote() throws IOException {
    String rcloneOutput = "2020/05/26 15:17:05 ERROR : 20200201_090433.jpg: File not in Encrypted drive 'Dropbox:/backup'";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    DiffModel model = new DiffModel();
    model.setSource(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.setTarget(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(runtime, model);
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(model.getTargetOnly().isEmpty());
    assertTrue(model.getContentDifferent().isEmpty());
    List<SyncFile> notInRemote = model.getSourceOnly();
    assertEquals(1, notInRemote.size());

    SyncFile syncFile = notInRemote.get(0);
    assertEquals("20200201_090433.jpg", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup", syncFile.getTargetPath());
  }

  /**
   * Tests if files which are on the local and remote side but are different are parsed correctly.
   */
  @Test
  void different() throws IOException {
    String rcloneOutput = "2020/05/26 15:17:06 ERROR : 20200108_184311.jpg: Sizes differ";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    DiffModel model = new DiffModel();
    model.setSource(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.setTarget(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(runtime, model);
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(model.getTargetOnly().isEmpty());
    assertTrue(model.getSourceOnly().isEmpty());

    List<SyncFile> sizeDiffer = model.getContentDifferent();
    assertEquals(1, sizeDiffer.size());

    SyncFile syncFile = sizeDiffer.get(0);
    assertEquals("20200108_184311.jpg", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup", syncFile.getTargetPath());
  }

  /**
   * Tests if the return code was set.
   */
  @Test
  void return_code() throws IOException {
    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[] {})).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    DiffModel model = new DiffModel();
    model.setSource(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.setTarget(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(runtime, model);
    checkCommand.createTask().run();

    assertEquals(0, checkCommand.getReturnCode());
  }
}
