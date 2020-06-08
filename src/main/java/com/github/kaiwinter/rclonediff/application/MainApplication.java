package com.github.kaiwinter.rclonediff.application;

import java.io.IOException;

import com.github.kaiwinter.rclonediff.DiffController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    AnchorPane page = (AnchorPane) loader.load(getClass().getResourceAsStream("/RcloneDiff.fxml"));
    Scene scene = new Scene(page);
    DiffController controller = loader.getController();
    controller.getLocalPath().setText("c:/temp/rclone-vs/2020/");
    controller.getRemotePath().setText("DropboxTineCrypt:/2020/");

    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setTitle("rclone visual diff");
  }
}
