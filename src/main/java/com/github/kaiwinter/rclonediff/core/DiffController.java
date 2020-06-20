package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;
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
  private ListView<LocalOnlyFile> localOnly;

  @Getter
  @FXML
  private ListView<String> diffs;

  @Getter
  @FXML
  private ListView<RemoteOnlyFile> remoteOnly;

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

  private Path tempDirectory;

  private RcloneService rcloneService;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    localOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter<LocalOnlyFile>()));
    remoteOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter<RemoteOnlyFile>()));

    localOnly.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalOnlyFile>() {

      @Override
      public void changed(ObservableValue<? extends LocalOnlyFile> observable, LocalOnlyFile oldValue, LocalOnlyFile newValue) {
        if (newValue == null) {
          localOnlyImage.setImage(null);
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

    remoteOnly.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RemoteOnlyFile>() {

      @Override
      public void changed(ObservableValue<? extends RemoteOnlyFile> observable, RemoteOnlyFile oldValue, RemoteOnlyFile newValue) {
        if (newValue == null) {
          remoteOnlyImage.setImage(null);
          return;
        }

        rcloneService.setRunnable(() -> {
          copy_internal(newValue);
        });
        rcloneService.restart();
      }

      private void copy_internal(RemoteOnlyFile newValue) {
        // TODO: better filetype filter
        if (!newValue.getFile().endsWith(".jpg")) {
          // TODO: show placeholder
          remoteOnlyImage.setImage(null);
          return;
        }

        try {
          Path completeFilePath = getTempDirectoryLazy().resolve(newValue.getFile());
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
          remoteOnlyImage.setImage(image);

        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

  }

  @FXML
  public void diff(ActionEvent event) {
    rcloneService.setRunnable(() -> {
      diff_internal();
    });
    rcloneService.restart();
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

  private Path getTempDirectoryLazy() throws IOException {
    if (tempDirectory == null) {
      tempDirectory = Files.createTempDirectory("rclone-diff");
    }
    return tempDirectory;
  }

  public void setService(RcloneService rcloneService) {
    this.rcloneService = rcloneService;
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
