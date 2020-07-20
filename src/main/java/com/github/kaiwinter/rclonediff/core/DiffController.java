package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
  private TextField localPath;

  @Getter
  @FXML
  private TextField remotePath;

  @Getter
  @FXML
  private ListView<SyncFile> localOnly;

  @Getter
  @FXML
  private ListView<SyncFile> diffs;

  @Getter
  @FXML
  private ListView<SyncFile> remoteOnly;

  @FXML
  private ImageView localOnlyImage;

  @FXML
  private ImageView remoteOnlyImage;

  @FXML
  private Label localOnlyLabel;

  @FXML
  private Label diffsLabel;

  @FXML
  private Label remoteOnlyLabel;

  @Getter
  @FXML
  private Button localChooseButton;

  @Getter
  @FXML
  private Button remoteChooseButton;

  @Getter
  @FXML
  private Button diffButton;

  private static Path tempDirectory;

  private RcloneCopyService rcloneCopyService;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    rcloneCopyService = new RcloneCopyService(getTempDirectoryLazy());

    localOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));
    remoteOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));

    rcloneCopyService.setOnSucceeded(event -> {
      remoteOnlyImage.setImage(rcloneCopyService.getLoadedImage());
      event.consume();
    });

    diffs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SyncFile>() {

      @Override
      public void changed(ObservableValue<? extends SyncFile> observable, SyncFile oldValue, SyncFile newValue) {
        localOnlyImage.setImage(null);
        remoteOnlyImage.setImage(null);
        localOnly.getSelectionModel().clearSelection();
        remoteOnly.getSelectionModel().clearSelection();
        if (newValue == null) {
          return;
        }
        Image image = new Image("file:///" + newValue.getLocalPath() + newValue.getFile());
        boolean error = image.isError();
        if (error) {
          log.error("Fehler beim Laden des Bildes");
        }
        localOnlyImage.setImage(image);

        rcloneCopyService.restart(newValue);
      }
    });

    localOnly.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SyncFile>() {

      @Override
      public void changed(ObservableValue<? extends SyncFile> observable, SyncFile oldValue, SyncFile newValue) {
        localOnlyImage.setImage(null);
        diffs.getSelectionModel().clearSelection();
        if (newValue == null) {
          return;
        }
        Image image = new Image("file:///" + newValue.getLocalPath() + newValue.getFile());
        boolean error = image.isError();
        if (error) {
          log.error("Fehler beim Laden des Bildes");
        }
        localOnlyImage.setImage(image);
      }
    });

    remoteOnly.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SyncFile>() {

      @Override
      public void changed(ObservableValue<? extends SyncFile> observable, SyncFile oldValue, SyncFile newValue) {
        remoteOnlyImage.setImage(null);
        diffs.getSelectionModel().clearSelection();
        if (newValue == null) {
          return;
        }

        rcloneCopyService.restart(newValue);
      }

    });

  }

  @FXML
  public void diff(ActionEvent event) {
    diff_internal();
  }

  private void diff_internal() {
    RcloneCheckService rcloneCheckService = new RcloneCheckService(localPath.getText(), remotePath.getText());

    // scene.getRoot().cursorProperty().bind(Bindings.when(rcloneCheckService.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT));
    localPath.disableProperty().bind(rcloneCheckService.runningProperty());
    remotePath.disableProperty().bind(rcloneCheckService.runningProperty());
    localChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    remoteChooseButton.disableProperty().bind(rcloneCheckService.runningProperty());
    diffButton.disableProperty().bind(rcloneCheckService.runningProperty());
    localOnly.disableProperty().bind(rcloneCheckService.runningProperty());
    remoteOnly.disableProperty().bind(rcloneCheckService.runningProperty());
    diffs.disableProperty().bind(rcloneCheckService.runningProperty());

    rcloneCheckService.setOnSucceeded(event -> {
      localOnly.setItems(FXCollections.observableArrayList(rcloneCheckService.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(rcloneCheckService.getSizeDiffer()));
      remoteOnly.setItems(FXCollections.observableArrayList(rcloneCheckService.getNotInLocal()));

      localOnlyLabel.setText("Local only (" + rcloneCheckService.getNotInRemote().size() + ")");
      diffsLabel.setText("Different content (" + rcloneCheckService.getSizeDiffer().size() + ")");
      remoteOnlyLabel.setText("Remote only (" + rcloneCheckService.getNotInLocal().size() + ")");
    });
    rcloneCheckService.start();

  }

  static synchronized Path getTempDirectoryLazy() {
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
