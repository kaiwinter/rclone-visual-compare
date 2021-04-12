package com.github.kaiwinter.rclonediff.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.RcloneCompareViewModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
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
public class RcloneCompareService {

  private final RcloneCommandlineServiceFactory serviceFactory;

  private Path tempDirectory;

  /**
   * Deletes the selected file on the source side.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void deleteSourceFile(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedSourceFiles();
    for (SyncFile syncFile : syncFiles) {
      boolean delete = model.isAlwaysDelete();
      if (!delete) {
        delete = askForDeleteConfirmation(model, syncFile.getFile());
      }

      if (delete) {
        DeleteCommand deleteCommand = new DeleteCommand(syncFile.getSourceEndpoint().getPath() + syncFile.getFile());
        deleteCommand.setCommandSucceededEvent(() -> {
          model.getSourceOnly().remove(syncFile);
        });

        serviceFactory.createServiceAndStart(deleteCommand);
      }
    }
  }

  /**
   * Deletes the selected file on the target side.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void deleteTargetFile(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedTargetFiles();
    for (SyncFile syncFile : syncFiles) {
      boolean delete = model.isAlwaysDelete();
      if (!delete) {
        delete = askForDeleteConfirmation(model, syncFile.getFile());
      }

      if (delete) {
        DeleteCommand deleteCommand = new DeleteCommand(syncFile.getTargetEndpoint().getPath() + syncFile.getFile());
        deleteCommand.setCommandSucceededEvent(() -> {
          model.getTargetOnly().remove(syncFile);
        });

        serviceFactory.createServiceAndStart(deleteCommand);
      }

    }
  }

  private boolean askForDeleteConfirmation(RcloneCompareViewModel model, String filename) {
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
   *          the {@link RcloneCompareViewModel}
   */
  public void copyToTarget(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedSourceFiles();
    for (SyncFile syncFile : syncFiles) {
      CopyCommand copyCommand = new CopyCommand(syncFile);
      copyCommand.setCommandSucceededEvent(() -> {
        model.getSourceOnly().remove(syncFile);
      });
      serviceFactory.createServiceAndStart(copyCommand);
    }
  }

  /**
   * Called to copy a file selected on the target side to the source side.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void copyToSource(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedTargetFiles();
    for (SyncFile syncFile : syncFiles) {
      SyncFile syncFileInverse = new SyncFile(syncFile.getTargetEndpoint(), syncFile.getSourceEndpoint(), syncFile.getFile());
      CopyCommand copyCommand = new CopyCommand(syncFileInverse);
      copyCommand.setCommandSucceededEvent(() -> {
        model.getTargetOnly().remove(syncFile);
      });
      serviceFactory.createServiceAndStart(copyCommand);
    }
  }

  /**
   * Called to copy a file selected in the "different content" list to the target side.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void copyToTargetFromDiff(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedDiffFiles();
    for (SyncFile syncFile : syncFiles) {
      CopyCommand copyCommand = new CopyCommand(syncFile);
      copyCommand.setCommandSucceededEvent(() -> {
        model.getContentDifferent().remove(syncFile);
      });
      serviceFactory.createServiceAndStart(copyCommand);
    }
  }

  /**
   * Called to copy a file selected in the "different content" list to the source side.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void copyToSourceFromDiff(RcloneCompareViewModel model) {
    ObservableList<SyncFile> syncFiles = model.getSelectedDiffFiles();
    for (SyncFile syncFile : syncFiles) {
      SyncFile syncFileInverse = new SyncFile(syncFile.getTargetEndpoint(), syncFile.getSourceEndpoint(), syncFile.getFile());
      CopyCommand copyCommand = new CopyCommand(syncFileInverse);
      copyCommand.setCommandSucceededEvent(() -> {
        model.getContentDifferent().remove(syncFile);
      });
      serviceFactory.createServiceAndStart(copyCommand);
    }
  }

  /**
   * Called to show an image which is selected on the source side.
   *
   * @param syncFile
   *          the {@link SyncFile} which should be shown
   * @param imageViewImage
   *          the Property which gets filled with the image
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void showImageFromSourcePath(SyncFile syncFile, ObjectProperty<Image> imageViewImage, RcloneCompareViewModel model) {
    imageViewImage.set(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    SyncEndpoint syncEndpoint = syncFile.getSourceEndpoint();

    if (syncEndpoint.getType() == Type.LOCAL) {
      Path completeFilePath = Path.of(syncEndpoint.getPath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, imageViewImage);
    } else {
      showRemoteFile(new SyncFile(syncEndpoint, new SyncEndpoint(Type.LOCAL, getTempDirectoryLazy().toString()), syncFile.getFile()),
        imageViewImage, model);
    }
  }

  /**
   * Called to show an image which is selected on the target side.
   *
   * @param syncFile
   *          the {@link SyncFile} which should be shown
   * @param imageViewImage
   *          the Property which gets filled with the image
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public void showImageFromTargetPath(SyncFile syncFile, ObjectProperty<Image> imageViewImage, RcloneCompareViewModel model) {
    imageViewImage.set(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    SyncEndpoint syncEndpoint = syncFile.getTargetEndpoint();

    if (syncEndpoint.getType() == Type.LOCAL) {
      Path completeFilePath = Path.of(syncEndpoint.getPath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, imageViewImage);
    } else {
      showRemoteFile(new SyncFile(syncEndpoint, new SyncEndpoint(Type.LOCAL, getTempDirectoryLazy().toString()), syncFile.getFile()),
        imageViewImage, model);
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

  private void showRemoteFile(SyncFile syncFile, ObjectProperty<Image> imageViewImage, RcloneCompareViewModel model) {
    model.setLatestCopyCommand(null);
    Path completeFilePath = Path.of(syncFile.getTargetEndpoint().getPath()).resolve(syncFile.getFile());
    if (completeFilePath.toFile().exists()) {
      showLocalFile(completeFilePath, imageViewImage);
      return;
    }
    CopyCommand rcloneCopyService = new CopyCommand(syncFile);
    model.setLatestCopyCommand(rcloneCopyService);
    rcloneCopyService.setCommandSucceededEvent(() -> {

      if (rcloneCopyService == model.getLatestCopyCommand()) {
        showLocalFile(completeFilePath, imageViewImage);
      }
    });
    serviceFactory.createServiceAndStart(rcloneCopyService);
  }

  private boolean isImage(SyncFile syncFile) {
    String fileExtension = syncFile.getFile().toLowerCase();
    return fileExtension.endsWith(".jpg") //
      || fileExtension.endsWith(".jpeg") //
      || fileExtension.endsWith(".png") //
      || fileExtension.endsWith(".gif") //
      || fileExtension.endsWith(".bmp");
  }

  Path getTempDirectoryLazy() {
    if (tempDirectory == null) {
      try {
        tempDirectory = Files.createTempDirectory("rclone-diff");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return tempDirectory;
  }

  /**
   * Deletes the temporary directory.
   */
  public void deleteTempDirectory() {
    if (tempDirectory != null) {
      try {
        FileUtils.deleteDirectory(tempDirectory.toFile());
      } catch (IOException e) {
        log.error("Couldn't delete temp directory '{}'", tempDirectory, e);
      }
    }
  }
}
