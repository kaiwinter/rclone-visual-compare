package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone copy command.
 */
@Slf4j
@RequiredArgsConstructor
public class CopyCommand extends AbstractCommand {

  private final Runtime runtime;
  private final SyncFile syncFile;

  @Override
  protected void execute() throws IOException {
    copyFileFromTo(syncFile.getFile(), syncFile.getSourcePath(), syncFile.getTargetPath());
  }

  private void copyFileFromTo(String file, String fromPath, String toPath) throws IOException {
    String targetDirectory = toPath + file;
    targetDirectory = targetDirectory.substring(0, targetDirectory.lastIndexOf("/") + 1);
    String command = "rclone copy \"" + fromPath + file + "\" \"" + targetDirectory + "\"";
    log.info("Copy command: {}", command);
    consoleLog.add(command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      log.info(line);
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
