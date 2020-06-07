package com.github.kaiwinter.rclonediff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

public class DiffController implements Initializable {

  @FXML
  private TextField localPath;

  @FXML
  private TextField remotePath;

  @FXML
  private ListView<LocalOnlyFile> localOnly;

  @FXML
  private ListView<String> diffs;

  @FXML
  private ListView<RemoteOnlyFile> remoteOnly;

  String localPathS = "c:/temp/rclone-vs/2020/";
  String remotePathS = "DropboxTineCrypt:/2020/";

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

    RcloneWrapper main = new RcloneWrapper();
    try {
      main.check(localPathS, remotePathS);
      localOnly.setItems(FXCollections.observableArrayList(main.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(main.getSizeDiffer()));
      remoteOnly.setItems(FXCollections.observableArrayList(main.getNotInLocal()));

    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
