package com.github.kaiwinter.rclonediff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.github.kaiwinter.rclonediff.model.NotInLocal;
import com.github.kaiwinter.rclonediff.model.NotInRemote;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

public class DiffController implements Initializable {

  @FXML
  private ListView<NotInRemote> localOnly;

  @FXML
  private ListView<String> diffs;

  @FXML
  private ListView<NotInLocal> remoteOnly;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    localOnly.setCellFactory(TextFieldListCell.forListView(new StringConverter<NotInRemote>() {

      @Override
      public String toString(NotInRemote object) {
        return object.getRemotePath() + "/" + object.getFile();
      }

      @Override
      public NotInRemote fromString(String string) {
        // TODO Auto-generated method stub
        return null;
      }
    }));

    remoteOnly.setCellFactory(TextFieldListCell.forListView(new StringConverter<NotInLocal>() {

      @Override
      public String toString(NotInLocal object) {
        return object.getLocalPath() + "/" + object.getFile();
      }

      @Override
      public NotInLocal fromString(String string) {
        // TODO Auto-generated method stub
        return null;
      }
    }));

    System.out.println();
    RcloneWrapper main = new RcloneWrapper();
    try {
      main.check();
      localOnly.setItems(FXCollections.observableArrayList(main.getNotInRemote()));
      diffs.setItems(FXCollections.observableArrayList(main.getSizeDiffer()));
      remoteOnly.setItems(FXCollections.observableArrayList(main.getNotInLocal()));

    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
