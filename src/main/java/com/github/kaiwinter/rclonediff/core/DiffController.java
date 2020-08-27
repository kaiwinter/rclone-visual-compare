package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.command.CheckCommand;
import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiffController implements Initializable {

  @Getter
  @FXML
  private TextField sourcePath;

  @Getter
  @FXML
  private TextField targetPath;

  @Getter
  @FXML
  private ListView<SyncFile> sourceOnly;

  @Getter
  @FXML
  private ListView<SyncFile> diffs;

  @Getter
  @FXML
  private ListView<SyncFile> targetOnly;

  @FXML
  private ImageView sourceOnlyImage;

  @FXML
  private ImageView targetOnlyImage;

  @FXML
  private Label sourceOnlyLabel;

  @FXML
  private Label diffsLabel;

  @FXML
  private Label targetOnlyLabel;

  @Getter
  @FXML
  private Button sourceChooseButton;

  @Getter
  @FXML
  private Button targetChooseButton;

  @Getter
  @FXML
  private Button diffButton;

  @FXML
  private Button sourceDeleteFileButton;

  @FXML
  private Button targetDeleteFileButton;

  @FXML
  private ProgressIndicator progressIndicator;

  private Path tempDirectory;

  private DiffModel model = new DiffModel();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    sourceOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));
    diffs.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));
    targetOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));

    diffs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      showImageFromSourcePath(newValue);
      showImageFromTargetPath(newValue);
    });

    sourceOnly.getSelectionModel().selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> showImageFromSourcePath(newValue));
    targetOnly.getSelectionModel().selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> showImageFromTargetPath(newValue));

    sourceDeleteFileButton.disableProperty().bind(Bindings.isEmpty(sourceOnly.getSelectionModel().getSelectedItems()));
    targetDeleteFileButton.disableProperty().bind(Bindings.isEmpty(targetOnly.getSelectionModel().getSelectedItems()));

    sourcePath.textProperty().bind(model.getSource().getPath());
    targetPath.textProperty().bind(model.getTarget().getPath());

    sourceOnly.setItems(model.getSourceOnly());
    diffs.setItems(model.getContentDifferent());
    targetOnly.setItems(model.getTargetOnly());
  }

  private void showImageFromSourcePath(SyncFile syncFile) {
    sourceOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = sourcePath.getText();

    if (isLocalPath(path)) {
      showLocalFile(syncFile.getSourcePath() + "/" + syncFile.getFile(), sourceOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getSourcePath(), getTempDirectoryLazy().toString(), syncFile.getFile()), sourceOnlyImage);
    }
  }

  private void showImageFromTargetPath(SyncFile syncFile) {
    targetOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = targetPath.getText();

    if (isLocalPath(path)) {
      showLocalFile(syncFile.getTargetPath() + "/" + syncFile.getFile(), targetOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getTargetPath(), getTempDirectoryLazy().toString(), syncFile.getFile()), targetOnlyImage);
    }
  }

  private boolean isLocalPath(String path) {
    try {
      Paths.get(path);
    } catch (InvalidPathException | NullPointerException ex) {
      return false;
    }
    return true;
  }

  private void showLocalFile(String absoluteFilename, ImageView targetImageView) {
    Image image = new Image("file:///" + absoluteFilename/* , true */);
    boolean error = image.isError();
    if (error) {
      log.error("Fehler beim Laden des Bildes");
    }
    targetImageView.setImage(image);
  }

  private void showRemoteFile(SyncFile syncFile, ImageView targetImageView) {
    CopyCommand rcloneCopyService = new CopyCommand(Runtime.getRuntime(), syncFile);
    rcloneCopyService.setOnSucceeded(event -> {
      if (rcloneCopyService.isLatestCopyCommand()) {
        targetImageView.setImage(rcloneCopyService.getLoadedImage());
        event.consume();
      }
    });
    rcloneCopyService.start();
  }

  @FXML
  public void diff() {
    model.getSourceOnly().clear();
    model.getContentDifferent().clear();
    model.getTargetOnly().clear();

    CheckCommand rcloneCheckService = new CheckCommand(Runtime.getRuntime(), model);

    sourcePath.disableProperty().bind(rcloneCheckService.runningProperty());
    targetPath.disableProperty().bind(rcloneCheckService.runningProperty());
    sourceChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    targetChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    diffButton.disableProperty().bind(rcloneCheckService.runningProperty());
    progressIndicator.visibleProperty().bind(rcloneCheckService.runningProperty());
    sourceDeleteFileButton.disableProperty().bind(rcloneCheckService.runningProperty());
    targetDeleteFileButton.disableProperty().bind(rcloneCheckService.runningProperty());

    rcloneCheckService.setOnSucceeded(event -> {
      sourceOnlyLabel.setText("Local only (" + sourceOnly.getItems().size() + ")");
      diffsLabel.setText("Different content (" + diffs.getItems().size() + ")");
      targetOnlyLabel.setText("Remote only (" + targetOnly.getItems().size() + ")");
    });
    rcloneCheckService.start();
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

  /**
   * Deletes the selected file on the source side.
   */
  @FXML
  public void deleteSourceFile() {
    SyncFile syncFile = sourceOnly.getSelectionModel().selectedItemProperty().get();
    DeleteCommand deleteCommand = new DeleteCommand(Runtime.getRuntime(), sourcePath.getText() + "/" + syncFile.getFile());
    deleteCommand.start();
  }

  /**
   * Deletes the selected file on the target side.
   */
  @FXML
  public void deleteTargetFile() {
    SyncFile syncFile = targetOnly.getSelectionModel().selectedItemProperty().get();
    DeleteCommand deleteCommand = new DeleteCommand(Runtime.getRuntime(), targetPath.getText() + "/" + syncFile.getFile());
    deleteCommand.start();
  }
}
