package com.github.kaiwinter.rclonediff.command;

import lombok.Getter;
import lombok.Setter;

/**
 * Parent class for rclone commands. The implementation have to provide the command line which gets
 * run ({@link #getCommandline()}) and have to handle the output of the rclone command line-wise by
 * {@link #handleRcloneOutput(String)}.
 */
public abstract class AbstractCommand {

  @Setter
  @Getter
  private Runnable commandSucceededEvent;

  /**
   * @return the command line which gets executed by this command
   */
  public abstract String getCommandline();

  /**
   * This method takes the output of the rclone command (line-wise) and interprets it for this
   * command.
   *
   * @param line
   *          the current output
   */
  protected abstract void handleRcloneOutput(String line);

  /**
   * A successful rclone command returns mostly a 0 as successful return code. But some commands
   * return different codes for success.
   *
   * @return the return codes of the successful rclone run
   */
  public abstract int[] getExpectedReturnCodes();
}
