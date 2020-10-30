package com.github.kaiwinter.rclonediff.command;

import static com.github.kaiwinter.rclonediff.util.StringUtils.wrapInQuotes;

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
  protected String getCommandline() {
    String file = syncFile.getFile();
    String fromPath = syncFile.getSourcePath();
    String toPath = syncFile.getTargetPath();
    String targetDirectory = toPath + file;
    targetDirectory = targetDirectory.substring(0, targetDirectory.lastIndexOf("/") + 1);
    return "copy " + wrapInQuotes(fromPath + file) + " " + wrapInQuotes(targetDirectory);
  }

  @Override
  protected void handleRcloneOutput(String line) {
    // noop
  }
}
