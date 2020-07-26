package com.github.kaiwinter.rclonediff.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.concurrent.Service;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Service} which calls a rclone check command.
 */
@Slf4j
@RequiredArgsConstructor
public class RcloneCopyService extends RcloneService {

  private final SyncFile syncFile;
  private final Path tempDirectory;

  @Getter
  private Image loadedImage;

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
      copyFileFromTo(syncFile.getFile(), syncFile.getRemotePath(), completeFilePath.getParent().toString());
    }

    URI filename = completeFilePath.toUri();
    log.info("Showing file: {}", filename);
    Image image = new Image(filename.toString());
    boolean error = image.isError();
    if (error) {
      log.error("Fehler beim Laden des Bildes");
    }
    this.loadedImage = image;
  }

  private static void copyFileFromTo(String file, String fromPath, String toPath) throws IOException {
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
}