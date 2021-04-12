package com.github.kaiwinter.rclonediff.service;

import static com.github.kaiwinter.rclonediff.util.TestFactories.SyncEndpointFactory.createLocalEndpoint;
import static com.github.kaiwinter.rclonediff.util.TestFactories.SyncEndpointFactory.createRemoteEndpoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.kaiwinter.rclonediff.command.AbstractCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.RcloneCompareViewModel;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;

/**
 * Tests for {@link RcloneCompareService}.
 */
class DiffServiceTest {

  /**
   * Necessary to test the construction of an {@link Image} object.
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

  @Test
  void deleteSourceFile() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).deleteSourceFile(model);

    assertEquals(0, model.getSourceOnly().size());
    assertEquals("delete \"source/file\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void deleteSourceFile_fail() {
    AlwaysFailingServiceFactory serviceFactory = new AlwaysFailingServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).deleteSourceFile(model);

    assertEquals(1, model.getSourceOnly().size());
  }

  @Test
  void deleteTargetFile() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getTargetOnly().add(synFileToCopy);
    model.setSelectedTargetFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).deleteTargetFile(model);

    assertEquals(0, model.getTargetOnly().size());
    assertEquals("delete \"target/file\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToTarget() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).copyToTarget(model);

    assertEquals(0, model.getSourceOnly().size());
    assertEquals("copy \"source/file\" \"target/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToTarget_fail() {
    AlwaysFailingServiceFactory serviceFactory = new AlwaysFailingServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).copyToTarget(model);

    assertEquals(1, model.getSourceOnly().size());
  }

  @Test
  void copyToSource() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getTargetOnly().add(synFileToCopy);
    model.setSelectedTargetFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).copyToSource(model);

    assertEquals(0, model.getTargetOnly().size());
    assertEquals("copy \"target/file\" \"source/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToTargetFromDiff() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getContentDifferent().add(synFileToCopy);
    model.setSelectedDiffFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).copyToTargetFromDiff(model);

    assertEquals(0, model.getContentDifferent().size());
    assertEquals("copy \"source/file\" \"target/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToSourceFromDiff() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    SyncFile synFileToCopy = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint("target"), "file");
    model.getContentDifferent().add(synFileToCopy);
    model.setSelectedDiffFiles(FXCollections.observableList(List.of(synFileToCopy)));
    new RcloneCompareService(serviceFactory).copyToSourceFromDiff(model);

    assertEquals(0, model.getContentDifferent().size());
    assertEquals("copy \"target/file\" \"source/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void showImageFromSourcePath_local() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    String file = "image.png";
    String pathToFile = new File(DiffServiceTest.class.getResource(file).getFile()).getParentFile().getAbsolutePath();
    SyncFile syncFile = new SyncFile(createLocalEndpoint(pathToFile), createLocalEndpoint("target"), file);
    new RcloneCompareService(serviceFactory).showImageFromSourcePath(syncFile, model.sourceImageProperty(), model);

    assertNotNull(model.getSourceImage());
    assertNull(model.getTargetImage());
  }

  @Test
  void showImageFromTargetPath_local() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    RcloneCompareViewModel model = new RcloneCompareViewModel();
    String file = "image.png";
    String pathToFile = new File(DiffServiceTest.class.getResource(file).getFile()).getParentFile().getAbsolutePath();
    SyncFile syncFile = new SyncFile(createLocalEndpoint("source"), createLocalEndpoint(pathToFile), file);
    new RcloneCompareService(serviceFactory).showImageFromTargetPath(syncFile, model.targetImageProperty(), model);

    assertNull(model.getSourceImage());
    assertNotNull(model.getTargetImage());
  }

  @Test
  void showImageFromSourcePath_remote() {
    RcloneCompareService diffService = new RcloneCompareService(new AlwaysSuccessfulServiceFactory(mock(Runtime.class)));
    Path tempDirectory = diffService.getTempDirectoryLazy();
    try {
      RcloneCompareViewModel model = new RcloneCompareViewModel();
      SyncFile syncFile = new SyncFile(createRemoteEndpoint("Remotename:/"), createLocalEndpoint(tempDirectory.toString()), "image.png");
      diffService.showImageFromSourcePath(syncFile, model.sourceImageProperty(), model);

      assertNotNull(model.getSourceImage());
      assertNull(model.getTargetImage());

      String expectedCommandLine = "copy \"Remotename:/image.png\" \"" + tempDirectory.toString() + "/\"";
      String actualCommandLine = model.getLatestCopyCommand().getCommandline();
      assertEquals(expectedCommandLine, actualCommandLine);
    } finally {
      diffService.deleteTempDirectory();
    }
  }

  @Test
  void showImageFromTargetPath_remote() {
    RcloneCompareService diffService = new RcloneCompareService(new AlwaysSuccessfulServiceFactory(mock(Runtime.class)));
    Path tempDirectory = diffService.getTempDirectoryLazy();
    try {
      RcloneCompareViewModel model = new RcloneCompareViewModel();
      SyncFile syncFile = new SyncFile(createLocalEndpoint("source"), createRemoteEndpoint("Remotename:/"), "image.png");
      diffService.showImageFromTargetPath(syncFile, model.targetImageProperty(), model);

      assertNull(model.getSourceImage());
      assertNotNull(model.getTargetImage());

      String expectedCommandLine = "copy \"Remotename:/image.png\" \"" + tempDirectory.toString() + "/\"";
      String actualCommandLine = model.getLatestCopyCommand().getCommandline();
      assertEquals(expectedCommandLine, actualCommandLine);
    } finally {
      diffService.deleteTempDirectory();
    }
  }

  /**
   * A RcloneCommandlineServiceFactory which doesn't actually executes the command but just call its
   * commandSucceededEvent method. Note: Not thread-safe each test should use its own instance if run
   * in parallel.
   */
  private static class AlwaysSuccessfulServiceFactory extends RcloneCommandlineServiceFactory {

    private AbstractCommand lastCommand;

    public AlwaysSuccessfulServiceFactory(Runtime runtime) {
      super(runtime);
    }

    @Override
    public void createServiceAndStart(AbstractCommand command) {
      this.lastCommand = command;
      command.getCommandSucceededEvent().run();
    }
  }

  /**
   * A RcloneCommandlineServiceFactory which doesn't actually executes the command and also doesn't
   * call its commandSucceededEvent method.
   */
  private static class AlwaysFailingServiceFactory extends RcloneCommandlineServiceFactory {


    public AlwaysFailingServiceFactory(Runtime runtime) {
      super(runtime);
    }

    @Override
    public void createServiceAndStart(AbstractCommand command) {}
  }
}
