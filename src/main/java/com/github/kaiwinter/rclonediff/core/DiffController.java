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
import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

  private Path tempDirectory;

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
  }

  private void showImageFromSourcePath(SyncFile syncFile) {
    sourceOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = sourcePath.getText();

    if (isLocalPath(path)) {
      showLocalFile(path, syncFile, sourceOnlyImage);
    } else {
      showRemoteFile(path, syncFile, sourceOnlyImage);
    }
  }

  private void showImageFromTargetPath(SyncFile syncFile) {
    targetOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = targetPath.getText();

    if (isLocalPath(path)) {
      showLocalFile(path, syncFile, targetOnlyImage);
    } else {
      showRemoteFile(path, syncFile, targetOnlyImage);
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

  private void showLocalFile(String path, SyncFile syncFile, ImageView targetImageView) {
    Image image = new Image("file:///" + path + "/" + syncFile.getFile());
    boolean error = image.isError();
    if (error) {
      log.error("Fehler beim Laden des Bildes");
    }
    targetImageView.setImage(image);
  }

  private void showRemoteFile(String path, SyncFile syncFile, ImageView targetImageView) {
    CopyCommand rcloneCopyService = new CopyCommand(Runtime.getRuntime(), path, syncFile, getTempDirectoryLazy());
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
    CheckCommand rcloneCheckService = new CheckCommand(Runtime.getRuntime(), sourcePath.getText(), targetPath.getText());

    // scene.getRoot().cursorProperty().bind(Bindings.when(rcloneCheckService.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT));
    sourcePath.disableProperty().bind(rcloneCheckService.runningProperty());
    targetPath.disableProperty().bind(rcloneCheckService.runningProperty());
    sourceChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    targetChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    diffButton.disableProperty().bind(rcloneCheckService.runningProperty());
    sourceOnly.disableProperty().bind(rcloneCheckService.runningProperty());
    targetOnly.disableProperty().bind(rcloneCheckService.runningProperty());
    diffs.disableProperty().bind(rcloneCheckService.runningProperty());

    rcloneCheckService.setOnSucceeded(event -> {
      sourceOnly.setItems(FXCollections.observableArrayList(rcloneCheckService.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(rcloneCheckService.getSizeDiffer()));
      targetOnly.setItems(FXCollections.observableArrayList(rcloneCheckService.getNotInLocal()));

      sourceOnlyLabel.setText("Local only (" + rcloneCheckService.getNotInRemote().size() + ")");
      diffsLabel.setText("Different content (" + rcloneCheckService.getSizeDiffer().size() + ")");
      targetOnlyLabel.setText("Remote only (" + rcloneCheckService.getNotInLocal().size() + ")");
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

}
