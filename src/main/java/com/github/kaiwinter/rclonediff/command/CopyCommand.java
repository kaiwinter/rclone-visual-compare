package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone copy command.
 */
@Slf4j
public class CopyCommand extends AbstractCommand {

  private static final Object $LOCK = new Object[0];

  @Setter(onMethod_ = @Synchronized)
  private static CopyCommand latest;

  private final Runtime runtime;
  private final SyncFile syncFile;
  private final Path tempDirectory;

  @Getter
  private Image loadedImage;

  private String path;

  /**
   * Constructs a new {@link CopyCommand}.
   * 
   * @param runtime
   *          the {@link Runnable} to execute the rclone command
   * @param path
   * @param syncFile
   *          the {@link SyncFile} which contains informations about the file which should be copied.
   * @param tempDirectory
   *          directory to store temporary files
   */
  public CopyCommand(Runtime runtime, String path, SyncFile syncFile, Path tempDirectory) {
    this.runtime = runtime;
    this.path = path;
    this.syncFile = syncFile;
    this.tempDirectory = tempDirectory;
    CopyCommand.setLatest(this);
  }

  @Override
  protected void execute() throws IOException {
    // TODO: better filetype filter
    if (!syncFile.getFile().endsWith(".jpg")) {
      // TODO: show placeholder
      this.loadedImage = null;
      return;
    }

    Path completeFilePath = tempDirectory.resolve(syncFile.getFile());
    if (!completeFilePath.toFile().exists()) {
      copyFileFromTo(syncFile.getFile(), path, completeFilePath.getParent());
    }

    URI filename = completeFilePath.toUri();

    this.loadedImage = new Image(filename.toString());
    if (this.loadedImage.isError()) {
      log.error("Fehler beim Laden des Bildes");
    }
  }

  private void copyFileFromTo(String file, String fromPath, Path toPath) throws IOException {
    String command = "rclone copy " + fromPath + "/" + file + " " + toPath;
    log.info("Copy command: {}", command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      log.error(line);
    }
    wait(process);
    log.info("check value code {}", process.exitValue());
  }

  /**
   * @return <code>true</code> if this is the last {@link CopyCommand} which was started
   */
  public boolean isLatestCopyCommand() {
    return this == latest;
  }
}