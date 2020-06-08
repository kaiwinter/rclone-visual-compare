package com.github.kaiwinter.rclonediff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;
import com.github.kaiwinter.rclonediff.model.SyncFileStringConverter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

  @FXML
  private ListView<LocalOnlyFile> localOnly;

  @FXML
  private ListView<String> diffs;

  @FXML
  private ListView<RemoteOnlyFile> remoteOnly;

  @FXML
  private ImageView localOnlyImage;

  @FXML
  private ImageView remoteOnlyImage;

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

  }

  @FXML
  public void diff(ActionEvent event) {
    Task<Void> task = new Task<>() {
      @Override
      public Void call() {
        diff_internal();
        return null;
      }
    };
    task.setOnSucceeded(taskFinishEvent -> System.out.println(taskFinishEvent));
    new Thread(task).start();
  }

  private void diff_internal() {
    RcloneWrapper main = new RcloneWrapper();
    try {
      main.check(localPath.getText(), remotePath.getText());
      localOnly.setItems(FXCollections.observableArrayList(main.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(main.getSizeDiffer()));
      remoteOnly.setItems(FXCollections.observableArrayList(main.getNotInLocal()));

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

}
