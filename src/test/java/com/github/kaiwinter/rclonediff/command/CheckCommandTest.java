package com.github.kaiwinter.rclonediff.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;

/**
 * Tests for {@link CheckCommand}.
 */
class CheckCommandTest {

  /**
   * Necessary because the model uses a Observable List from JavaFX.
   */
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

  public static void waitForRunLater() {
    Semaphore semaphore = new Semaphore(0);
    Platform.runLater(() -> semaphore.release());
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Tests the generated rclone command.
   */
  @Test
  void valid_command() {
    DiffModel model = new DiffModel();
    model.getSource().setValue(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.getTarget().setValue(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(model);
    assertEquals("rclone check \"c:/temp/\" \"Dropbox:/backup\"", checkCommand.getCommandline());
  }

  /**
   * Tests if files which are not on local side are parsed correctly.
   */
  @Test
  void not_in_local() {
    String rcloneOutput = "2020/05/26 15:17:06 ERROR : 20200501_081347.mp4: File not in Local file system at //?/c:/temp/";

    DiffModel model = new DiffModel();
    model.getSource().setValue(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.getTarget().setValue(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(model);
    checkCommand.handleRcloneOutput(rcloneOutput);

    waitForRunLater();
    assertTrue(model.getSourceOnly().isEmpty());
    assertTrue(model.getContentDifferent().isEmpty());
    List<SyncFile> notInLocal = model.getTargetOnly();
    assertEquals(1, notInLocal.size());

    SyncFile syncFile = notInLocal.get(0);
    assertEquals("20200501_081347.mp4", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup/", syncFile.getTargetPath());
  }

  /**
   * Tests if files which are not the remote side are parsed correctly.
   */
  @Test
  void not_in_remote() {
    String rcloneOutput = "2020/05/26 15:17:05 ERROR : 20200201_090433.jpg: File not in Encrypted drive 'Dropbox:/backup'";

    DiffModel model = new DiffModel();
    model.getSource().setValue(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.getTarget().setValue(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(model);
    checkCommand.handleRcloneOutput(rcloneOutput);

    waitForRunLater();
    assertTrue(model.getTargetOnly().isEmpty());
    assertTrue(model.getContentDifferent().isEmpty());
    List<SyncFile> notInRemote = model.getSourceOnly();
    assertEquals(1, notInRemote.size());

    SyncFile syncFile = notInRemote.get(0);
    assertEquals("20200201_090433.jpg", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup/", syncFile.getTargetPath());
  }

  /**
   * Tests if files which are on the local and remote side but are different are parsed correctly.
   */
  @Test
  void different() {
    String rcloneOutput = "2020/05/26 15:17:06 ERROR : 20200108_184311.jpg: Sizes differ";

    DiffModel model = new DiffModel();
    model.getSource().setValue(new SyncEndpoint(SyncEndpoint.Type.LOCAL, "c:/temp/"));
    model.getTarget().setValue(new SyncEndpoint(SyncEndpoint.Type.REMOTE, "Dropbox:/backup"));

    CheckCommand checkCommand = new CheckCommand(model);
    checkCommand.handleRcloneOutput(rcloneOutput);

    waitForRunLater();
    assertTrue(model.getTargetOnly().isEmpty());
    assertTrue(model.getSourceOnly().isEmpty());

    List<SyncFile> sizeDiffer = model.getContentDifferent();
    assertEquals(1, sizeDiffer.size());

    SyncFile syncFile = sizeDiffer.get(0);
    assertEquals("20200108_184311.jpg", syncFile.getFile());
    assertEquals("c:/temp/", syncFile.getSourcePath());
    assertEquals("Dropbox:/backup/", syncFile.getTargetPath());
  }
}
