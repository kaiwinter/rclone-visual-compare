package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.concurrent.Service;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Service} which calls a rclone check command.
 */
@Slf4j
public class CopyCommand extends AbstractCommand {

  private static final Object $LOCK = new Object[0];

  @Setter(onMethod_ = @Synchronized)
  private static CopyCommand latest;

  private final SyncFile syncFile;
  private final Path tempDirectory;

  @Getter
  private Image loadedImage;

  public CopyCommand(SyncFile syncFile, Path tempDirectory) {
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
      copyFileFromTo(syncFile.getFile(), syncFile.getRemotePath(), completeFilePath.getParent());
    }

    URI filename = completeFilePath.toUri();

    this.loadedImage = new Image(filename.toString());
    if (this.loadedImage.isError()) {
      log.error("Fehler beim Laden des Bildes");
    }
  }

  private static void copyFileFromTo(String file, String fromPath, Path toPath) throws IOException {
    String command = "rclone copy " + fromPath + "/" + file + " " + toPath;
    log.info("Copy command: {}", command);

    Process process = Runtime.getRuntime().exec(command);
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