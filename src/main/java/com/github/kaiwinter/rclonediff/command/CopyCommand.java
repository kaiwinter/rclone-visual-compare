package com.github.kaiwinter.rclonediff.command;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import lombok.RequiredArgsConstructor;

/**
 * Executes a rclone copy command.
 */
@RequiredArgsConstructor
public class CopyCommand extends AbstractCommand {

  private final SyncFile syncFile;

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0};
  }

  @Override
  public String[] getCommandline() {
    String file = syncFile.getFile();
    String fromPath = syncFile.getSourceEndpoint().getPath();
    String toPath = syncFile.getTargetEndpoint().getPath();
    String targetDirectory = toPath + file;
    targetDirectory = targetDirectory.substring(0, targetDirectory.lastIndexOf("/"));

    return new String[] {"copy", fromPath + file, targetDirectory};
  }

  @Override
  protected void handleRcloneOutput(String line) {
    // noop
  }
}
