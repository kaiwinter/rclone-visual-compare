package com.github.kaiwinter.rclonediff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.github.kaiwinter.rclonediff.command.AbstractCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncFile;

/**
 * Tests for {@link DiffService}.
 */
class DiffServiceTest {

  @Test
  void deleteSourceFile() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFile(synFileToCopy);
    new DiffService(serviceFactory).deleteSourceFile(model);

    assertEquals(0, model.getSourceOnly().size());
  }

  @Test
  void deleteSourceFile_fail() {
    AlwaysFailingServiceFactory serviceFactory = new AlwaysFailingServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFile(synFileToCopy);
    new DiffService(serviceFactory).deleteSourceFile(model);

    assertEquals(1, model.getSourceOnly().size());
  }

  @Test
  void deleteTargetFile() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    model.setAlwaysDelete(true);
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getTargetOnly().add(synFileToCopy);
    model.setSelectedTargetFile(synFileToCopy);
    new DiffService(serviceFactory).deleteTargetFile(model);

    assertEquals(0, model.getTargetOnly().size());
  }

  @Test
  void copyToTarget() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFile(synFileToCopy);
    new DiffService(serviceFactory).copyToTarget(model);

    assertEquals(0, model.getSourceOnly().size());
    assertEquals("copy \"source/file\" \"target/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToTarget_fail() {
    AlwaysFailingServiceFactory serviceFactory = new AlwaysFailingServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getSourceOnly().add(synFileToCopy);
    model.setSelectedSourceFile(synFileToCopy);
    new DiffService(serviceFactory).copyToTarget(model);

    assertEquals(1, model.getSourceOnly().size());
  }

  @Test
  void copyToSource() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getTargetOnly().add(synFileToCopy);
    model.setSelectedTargetFile(synFileToCopy);
    new DiffService(serviceFactory).copyToSource(model);

    assertEquals(0, model.getTargetOnly().size());
    assertEquals("copy \"target/file\" \"source/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToTargetFromDiff() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getContentDifferent().add(synFileToCopy);
    model.setSelectedDiffFile(synFileToCopy);
    new DiffService(serviceFactory).copyToTargetFromDiff(model);

    assertEquals(0, model.getContentDifferent().size());
    assertEquals("copy \"source/file\" \"target/\"", serviceFactory.lastCommand.getCommandline());
  }

  @Test
  void copyToSourceFromDiff() {
    AlwaysSuccessfulServiceFactory serviceFactory = new AlwaysSuccessfulServiceFactory(mock(Runtime.class));

    DiffModel model = new DiffModel();
    SyncFile synFileToCopy = new SyncFile("source", "target", "file");
    model.getContentDifferent().add(synFileToCopy);
    model.setSelectedDiffFile(synFileToCopy);
    new DiffService(serviceFactory).copyToSourceFromDiff(model);

    assertEquals(0, model.getContentDifferent().size());
    assertEquals("copy \"target/file\" \"source/\"", serviceFactory.lastCommand.getCommandline());
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
    public void createServiceAndStart(String rcloneBinaryPath, AbstractCommand command) {
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
    public void createServiceAndStart(String rcloneBinaryPath, AbstractCommand command) {}
  }
}
