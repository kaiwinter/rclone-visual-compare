package com.github.kaiwinter.rclonediff.service;

import java.util.Optional;

import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import lombok.RequiredArgsConstructor;

/**
 * Methods gets called from the view in order to manipulate the ViewModel.
 */
@RequiredArgsConstructor
public class DiffService {

  private final RcloneCommandlineServiceFactory serviceFactory;

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

}
