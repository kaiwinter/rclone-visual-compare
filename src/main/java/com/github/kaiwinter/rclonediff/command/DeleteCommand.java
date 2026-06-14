package com.github.kaiwinter.rclonediff.command;

import lombok.RequiredArgsConstructor;

/**
 * Executes a rclone delete command.
 */
@RequiredArgsConstructor
public class DeleteCommand extends AbstractCommand {

  private final String absoluteFilename;

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0};
  }

  @Override
  public String[] getCommandline() {
    return new String[] {"delete", absoluteFilename};
  }

  @Override
  protected void handleRcloneOutput(String line) {
    // noop
  }
}
