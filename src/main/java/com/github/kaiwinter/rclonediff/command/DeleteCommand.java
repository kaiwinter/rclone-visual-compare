package com.github.kaiwinter.rclonediff.command;

import static com.github.kaiwinter.rclonediff.util.StringUtils.wrapInQuotes;

import lombok.RequiredArgsConstructor;

/**
 * Executes a rclone delete command.
 */
@RequiredArgsConstructor
public class DeleteCommand extends AbstractCommand {

  private final String rcloneBinaryPath;
  private final String absoluteFilename;

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0};
  }

  @Override
  protected String getCommandline() {
    return rcloneBinaryPath + " delete " + wrapInQuotes(absoluteFilename);
  }

  @Override
  protected void handleRcloneOutput(String line) {
    // noop
  }
}
