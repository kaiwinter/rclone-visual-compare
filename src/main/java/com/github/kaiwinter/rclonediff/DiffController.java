package com.github.kaiwinter.rclonediff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import lombok.Getter;

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
    localOnly.setCellFactory(TextFieldListCell.forListView(new StringConverter<LocalOnlyFile>() {

      @Override
      public String toString(LocalOnlyFile object) {
        return object.getFile() + ": " + object.getLocalPath() + " -> " + object.getRemotePath();
      }

      @Override
      public LocalOnlyFile fromString(String string) {
        // TODO Auto-generated method stub
        return null;
      }
    }));

    localOnly.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LocalOnlyFile>() {

      @Override
      public void changed(ObservableValue<? extends LocalOnlyFile> observable, LocalOnlyFile oldValue, LocalOnlyFile newValue) {
        System.out.println("Selected item: " + newValue);
        Image image = new Image("file:///" + newValue.getLocalPath() + newValue.getFile());
        boolean error = image.isError();
        if (error) {
          System.out.println("Fehler beim Laden des Bildes");
        }
        localOnlyImage.setImage(image);
      }
    });

    remoteOnly.setCellFactory(TextFieldListCell.forListView(new StringConverter<RemoteOnlyFile>() {

      @Override
      public String toString(RemoteOnlyFile object) {
        return object.getFile() + ": " + object.getRemotePath() + " -> " + object.getLocalPath();
      }

      @Override
      public RemoteOnlyFile fromString(String string) {
        // TODO Auto-generated method stub
        return null;
      }
    }));
  }

  @FXML
  public void diff(ActionEvent event) {
    RcloneWrapper main = new RcloneWrapper();
    try {
      main.check(localPath.getText(), remotePath.getText());
      localOnly.setItems(FXCollections.observableArrayList(main.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(main.getSizeDiffer()));
      remoteOnly.setItems(FXCollections.observableArrayList(main.getNotInLocal()));

    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
