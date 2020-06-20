package com.github.kaiwinter.rclonediff;

import java.io.IOException;

import com.github.kaiwinter.rclonediff.core.DiffController;
import com.github.kaiwinter.rclonediff.core.RcloneService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

  private final RcloneService service = new RcloneService();
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
    controller.getLocalPath().setText("c:/temp/rclone-vs/2020/");
    controller.getRemotePath().setText("DropboxTineCrypt:/2020/");
    controller.setService(service);

    scene.getRoot().cursorProperty().bind(Bindings.when(service.runningProperty()).then(Cursor.WAIT).otherwise(Cursor.DEFAULT));
    controller.getLocalPath().disableProperty().bind(service.runningProperty());
    controller.getRemotePath().disableProperty().bind(service.runningProperty());
    controller.getLocalChooseButton().disableProperty().bind(service.runningProperty());
    controller.getRemoteChooseButton().disableProperty().bind(service.runningProperty());
    controller.getDiffButton().disableProperty().bind(service.runningProperty());
    controller.getLocalOnly().disableProperty().bind(service.runningProperty());
    controller.getRemoteOnly().disableProperty().bind(service.runningProperty());
    controller.getDiffs().disableProperty().bind(service.runningProperty());

    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setTitle("rclone visual diff");
  }

  @Override
  public void stop() throws Exception {
    controller.deleteTempDirectory();
  }
}
