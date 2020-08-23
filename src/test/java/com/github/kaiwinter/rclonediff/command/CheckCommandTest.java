package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

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

    CheckCommand checkCommand = new CheckCommand(runtime, new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"),
      new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));
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

    CheckCommand checkCommand = new CheckCommand(runtime, new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"),
      new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(checkCommand.getNotInTarget().isEmpty());
    assertTrue(checkCommand.getSizeDiffer().isEmpty());
    List<SyncFile> notInLocal = checkCommand.getNotInSource();
    assertEquals(1, notInLocal.size());

    SyncFile syncFile = notInLocal.get(0);
    assertEquals("20200501_081347.mp4", syncFile.getFile());
    // assertEquals("c:/temp/", syncFile.getLocalPath());
    // assertEquals("Dropbox:/backup/", syncFile.getRemotePath());
  }

  /**
   * Tests if files which are not the remote side are parsed correctly.
   */
  @Test
  void not_in_remote() throws IOException {
    String rcloneOutput = "2020/05/26 15:17:05 ERROR : 20200201_090433.jpg: File not in Encrypted drive 'Dropbox:/backup/'";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    CheckCommand checkCommand = new CheckCommand(runtime, new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"),
      new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup/"));
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(checkCommand.getNotInSource().isEmpty());
    assertTrue(checkCommand.getSizeDiffer().isEmpty());
    List<SyncFile> notInRemote = checkCommand.getNotInTarget();
    assertEquals(1, notInRemote.size());

    SyncFile syncFile = notInRemote.get(0);
    assertEquals("20200201_090433.jpg", syncFile.getFile());
    // assertEquals("c:/temp/", syncFile.getLocalPath());
    // assertEquals("Dropbox:/backup/", syncFile.getRemotePath());
  }

  /**
   * Tests if files which are on the local and remote side but are different are parsed correctly.
   */
  @Test
  void different() throws IOException {
    String rcloneOutput = "2020/05/26 15:17:06 ERROR : 20200108_184311.jpg: Sizes differ";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    CheckCommand checkCommand = new CheckCommand(runtime, new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"),
      new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));
    Task<Void> task = checkCommand.createTask();
    task.run();
    Platform.runLater(() -> assertNull(task.getException()));

    assertTrue(checkCommand.getNotInSource().isEmpty());
    assertTrue(checkCommand.getNotInTarget().isEmpty());

    List<SyncFile> sizeDiffer = checkCommand.getSizeDiffer();
    assertEquals(1, sizeDiffer.size());

    SyncFile syncFile = sizeDiffer.get(0);
    assertEquals("20200108_184311.jpg", syncFile.getFile());
    // assertEquals("c:/temp/", syncFile.getLocalPath());
    // assertEquals("Dropbox:/backup/", syncFile.getRemotePath());
  }

  /**
   * Tests if an Exception is thrown if the rclone summary tells a different number of differences
   * than the number which were parsed.
   */
  @Test
  void parsed_too_less() throws IOException, InterruptedException, ExecutionException {
    String rcloneOutput =
      "2020/05/26 15:17:06 ERROR : 20200108_184311.jpg: Sizes differ\r\n2020/05/26 15:17:07 NOTICE: Encrypted drive 'Dropbox:/backup/': 2 differences found";

    Process process = when(mock(Process.class).getErrorStream()).thenReturn(new ByteArrayInputStream(rcloneOutput.getBytes())).getMock();
    Runtime runtime = when(mock(Runtime.class).exec(anyString())).thenReturn(process).getMock();

    CheckCommand checkCommand = new CheckCommand(runtime, new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"),
      new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));
    Task<Void> task = checkCommand.createTask();
    task.run();

    FutureTask<Throwable> futureTask = new FutureTask<>(() -> {
      return task.getException();
    });

    Platform.runLater(futureTask);
    Throwable data = futureTask.get();
    assertNotNull(data);

    assertTrue(checkCommand.getNotInSource().isEmpty());
    assertTrue(checkCommand.getNotInTarget().isEmpty());

    List<SyncFile> sizeDiffer = checkCommand.getSizeDiffer();
    assertEquals(1, sizeDiffer.size());

    SyncFile syncFile = sizeDiffer.get(0);
    assertEquals("20200108_184311.jpg", syncFile.getFile());
    // assertEquals("c:/temp/", syncFile.getLocalPath());
    // assertEquals("Dropbox:/backup/", syncFile.getRemotePath());
  }

}
