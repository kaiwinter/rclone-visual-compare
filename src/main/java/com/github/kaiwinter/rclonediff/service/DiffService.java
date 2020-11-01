package com.github.kaiwinter.rclonediff.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Methods gets called from the view in order to manipulate the ViewModel.
 */
@Slf4j
@RequiredArgsConstructor
public class DiffService {

  private final RcloneCommandlineServiceFactory serviceFactory;

  private Path tempDirectory;

  /**
   * Deletes the selected file on the source side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void deleteSourceFile(DiffModel model) {
    SyncFile syncFile = model.getSelectedSourceFile();
    boolean delete = model.isAlwaysDelete();
    if (!delete) {
      delete = askForDeleteConfirmation(model, syncFile.getFile());
    }

    if (delete) {
      DeleteCommand deleteCommand = new DeleteCommand(syncFile.getSourcePath() + syncFile.getFile());
      deleteCommand.setCommandSucceededEvent(() -> {
        model.getSourceOnly().remove(syncFile);
      });

      serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), deleteCommand);
    }
  }

  /**
   * Deletes the selected file on the target side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void deleteTargetFile(DiffModel model) {
    SyncFile syncFile = model.getSelectedTargetFile();
    boolean delete = model.isAlwaysDelete();
    if (!delete) {
      delete = askForDeleteConfirmation(model, syncFile.getFile());
    }

    if (delete) {
      DeleteCommand deleteCommand = new DeleteCommand(syncFile.getTargetPath() + syncFile.getFile());
      deleteCommand.setCommandSucceededEvent(() -> {
        model.getTargetOnly().remove(syncFile);
      });

      serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), deleteCommand);
    }
  }

  private boolean askForDeleteConfirmation(DiffModel model, String filename) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Delete confirmation");
    alert.setHeaderText("Do you really want to delete '" + filename + "'");
    alert.setContentText("Please confirm the deletion of the file. You can choose 'Yes, always' to suppress any further confirmations.");

    ButtonType buttonYes = new ButtonType("Yes");
    ButtonType buttonYesAlways = new ButtonType("Yes, always");
    ButtonType buttonNo = new ButtonType("No, cancel", ButtonData.CANCEL_CLOSE);

    alert.getButtonTypes().setAll(buttonYes, buttonYesAlways, buttonNo);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == buttonYes) {
      return true;
    } else if (result.get() == buttonYesAlways) {
      model.setAlwaysDelete(true);
      return true;
    }
    return false;
  }

  /**
   * Called to copy a file selected on the source side to the target side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void copyToTarget(DiffModel model) {
    SyncFile syncFile = model.getSelectedSourceFile();
    CopyCommand copyCommand = new CopyCommand(syncFile);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getSourceOnly().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to copy a file selected on the target side to the source side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void copyToSource(DiffModel model) {
    SyncFile syncFile = model.getSelectedTargetFile();
    SyncFile syncFileInverse = new SyncFile(syncFile.getTargetPath(), syncFile.getSourcePath(), syncFile.getFile());
    CopyCommand copyCommand = new CopyCommand(syncFileInverse);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getTargetOnly().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to copy a file selected in the "different content" list to the target side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void copyToTargetFromDiff(DiffModel model) {
    SyncFile syncFile = model.getSelectedDiffFile();
    CopyCommand copyCommand = new CopyCommand(syncFile);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getContentDifferent().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to copy a file selected in the "different content" list to the source side.
   *
   * @param model
   *          the {@link DiffModel}
   */
  public void copyToSourceFromDiff(DiffModel model) {
    SyncFile syncFile = model.getSelectedDiffFile();
    SyncFile syncFileInverse = new SyncFile(syncFile.getTargetPath(), syncFile.getSourcePath(), syncFile.getFile());
    CopyCommand copyCommand = new CopyCommand(syncFileInverse);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getContentDifferent().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to show an image which is selected on the source side.
   *
   * @param syncFile
   * @param imageViewImage
   * @param model
   */
  public void showImageFromSourcePath(SyncFile syncFile, ObjectProperty<Image> imageViewImage, DiffModel model) {
    imageViewImage.set(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    String path = syncFile.getSourcePath();

    if (isLocalPath(path)) {
      Path completeFilePath = Path.of(syncFile.getSourcePath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, imageViewImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getSourcePath(), getTempDirectoryLazy().toString(), syncFile.getFile()), imageViewImage, model);
    }
  }

  public void showImageFromTargetPath(SyncFile syncFile, ObjectProperty<Image> imageViewImage, DiffModel model) {
    imageViewImage.set(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    String path = syncFile.getTargetPath();

    if (isLocalPath(path)) {
      Path completeFilePath = Path.of(syncFile.getTargetPath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, imageViewImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getTargetPath(), getTempDirectoryLazy().toString(), syncFile.getFile()), imageViewImage, model);
    }
  }

  private void showLocalFile(Path absoluteFilename, ObjectProperty<Image> imageViewImage) {
    if (imageViewImage.get() != null) {
      imageViewImage.get().cancel();
    }

    Image image = new Image("file:///" + absoluteFilename, true);
    image.progressProperty().addListener((observable, oldValue, newValue) -> log.trace("Progress: " + newValue.doubleValue() * 100 + "%"));
    image.exceptionProperty().addListener((observable, oldValue, newValue) -> log.error(newValue.getMessage()));
    imageViewImage.set(image);
  }

  private void showRemoteFile(SyncFile syncFile, ObjectProperty<Image> imageViewImage, DiffModel model) {
    model.setLatestCopyCommand(null);
    Path completeFilePath = Path.of(syncFile.getTargetPath()).resolve(syncFile.getFile());
    if (completeFilePath.toFile().exists()) {
      showLocalFile(completeFilePath, imageViewImage);
      return;
    }
    CopyCommand rcloneCopyService = new CopyCommand(syncFile);
    rcloneCopyService.setCommandSucceededEvent(() -> {

      if (rcloneCopyService == model.getLatestCopyCommand()) {
        showLocalFile(completeFilePath, imageViewImage);
      }
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), rcloneCopyService);
    model.setLatestCopyCommand(rcloneCopyService);
  }

  private boolean isImage(SyncFile syncFile) {
    String fileExtension = syncFile.getFile().toLowerCase();
    return fileExtension.endsWith(".jpg") //
      || fileExtension.endsWith(".jpeg") //
      || fileExtension.endsWith(".png") //
      || fileExtension.endsWith(".gif") //
      || fileExtension.endsWith(".bmp");
  }

  private Path getTempDirectoryLazy() {
    if (tempDirectory == null) {
      try {
        tempDirectory = Files.createTempDirectory("rclone-diff");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return tempDirectory;
  }

  private boolean isLocalPath(String path) {
    try {
      Paths.get(path);
    } catch (InvalidPathException | NullPointerException ex) {
      return false;
    }
    return true;
  }

}
