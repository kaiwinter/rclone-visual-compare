package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone delete command.
 */
@Slf4j
@RequiredArgsConstructor
public class DeleteCommand extends AbstractCommand {

  private final Runtime runtime;
  private final String rcloneBinaryPath;
  private final String absoluteFilename;

  @Override
  protected void execute() throws IOException {
    String command = rcloneBinaryPath + " delete \"" + absoluteFilename + "\"";
    log.info("Delete command: {}", command);
    consoleLog.add(command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      log.error(line);
      consoleLog.add(line);
    }
    wait(process);

    returnCode = process.exitValue();
    log.info("rclone return code: {}", returnCode);
  }

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0};
  }
}
