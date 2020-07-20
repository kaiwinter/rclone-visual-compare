package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Service} which calls a rclone check command.
 */
@Slf4j
@RequiredArgsConstructor
public class RcloneCopyService extends Service<Void> {

  private final Path tempDirectory;

  @Getter
  private Image loadedImage;
  private SyncFile newValue;

  @Override
  protected Task<Void> createTask() {

    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        copy();
        return null;
      }
    };
  }

  private void copy() throws IOException {
    // TODO: better filetype filter
    if (!newValue.getFile().endsWith(".jpg")) {
      // TODO: show placeholder
      this.loadedImage = null;
      return;
    }

    Path completeFilePath = DiffController.getTempDirectoryLazy().resolve(newValue.getFile());
    if (!completeFilePath.toFile().exists()) {
      RcloneWrapper.copy(newValue.getFile(), newValue.getRemotePath(), completeFilePath.getParent().toString());
    }

    // String filename = "file:///" + completeFilePath.toString();
    URI filename = completeFilePath.toUri();
    log.info("Showing file: {}", filename);
    Image image = new Image(filename.toString());
    boolean error = image.isError();
    if (error) {
      log.error("Fehler beim Laden des Bildes");
    }
    this.loadedImage = image;

  }

  private static void wait(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void restart(SyncFile newValue) {
    super.cancel();
    this.newValue = newValue;
    super.restart();
  }
}