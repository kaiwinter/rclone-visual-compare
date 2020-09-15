package com.github.kaiwinter.rclonediff;

import java.io.IOException;

import com.github.kaiwinter.rclonediff.core.DiffController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

  private DiffController controller;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    AnchorPane page = (AnchorPane) loader.load(getClass().getResourceAsStream("/RcloneDiff.fxml"));
    Scene scene = new Scene(page);
    controller = loader.getController();

    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setTitle("rclone visual diff");
  }

  @Override
  public void stop() throws Exception {
    controller.deleteTempDirectory();
  }
}
