package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.github.kaiwinter.rclonediff.model.SyncFile;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone delete command.
 */
@Slf4j
public class DeleteCommand extends AbstractCommand {

  private final Runtime runtime;
  private final SyncFile syncFile;

  private String path;

  /**
   * Constructs a new {@link DeleteCommand}.
   * 
   * @param runtime
   *          the {@link Runnable} to execute the rclone command
   * @param path
   * @param syncFile
   *          the {@link SyncFile} which contains informations about the file which should be deleted.
   */
  public DeleteCommand(Runtime runtime, String path, SyncFile syncFile) {
    this.runtime = runtime;
    this.path = path;
    this.syncFile = syncFile;
  }

  @Override
  protected void execute() throws IOException {
    String command = "rclone delete \"" + path + "/" + syncFile.getFile() + "\"";
    log.info("Delete command: {}", command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      log.error(line);
    }
    wait(process);
    log.info("check value code {}", process.exitValue());
  }
}
