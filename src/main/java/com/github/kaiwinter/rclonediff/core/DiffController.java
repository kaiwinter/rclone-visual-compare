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
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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

  @FXML
  private Button sourceDeleteFileButton;

  @FXML
  private Button targetDeleteFileButton;

  @FXML
  private Button copyToTargetButton;

  @FXML
  private Button copyToSourceButton;

  @FXML
  private ProgressIndicator progressIndicator;

  private Path tempDirectory;

  private DiffModel model = new DiffModel();

  private CopyCommand latestCopyCommand;


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

    BooleanBinding sourceOnlyBinding = Bindings.isEmpty(sourceOnly.getSelectionModel().getSelectedItems());
    BooleanBinding targetOnlyBinding = Bindings.isEmpty(targetOnly.getSelectionModel().getSelectedItems());
    sourceDeleteFileButton.disableProperty().bind(sourceOnlyBinding);
    targetDeleteFileButton.disableProperty().bind(targetOnlyBinding);

    copyToTargetButton.disableProperty().bind(sourceOnlyBinding);
    copyToSourceButton.disableProperty().bind(targetOnlyBinding);

    sourcePath.textProperty().bind(model.getSource().getPath());
    targetPath.textProperty().bind(model.getTarget().getPath());

    sourceOnly.setItems(model.getSourceOnly());
    diffs.setItems(model.getContentDifferent());
    targetOnly.setItems(model.getTargetOnly());
  }

  private void showImageFromSourcePath(SyncFile syncFile) {
    sourceOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = sourcePath.getText();

    if (isLocalPath(path)) {
      showLocalFile(syncFile.getSourcePath() + "/" + syncFile.getFile(), sourceOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getSourcePath(), getTempDirectoryLazy().toString(), syncFile.getFile()), sourceOnlyImage);
    }
  }

  private void showImageFromTargetPath(SyncFile syncFile) {
    targetOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }

    String path = targetPath.getText();

    if (isLocalPath(path)) {
      showLocalFile(syncFile.getTargetPath() + "/" + syncFile.getFile(), targetOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getTargetPath(), getTempDirectoryLazy().toString(), syncFile.getFile()), targetOnlyImage);
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

  private void showLocalFile(String absoluteFilename, ImageView targetImageView) {
    Image currentImage = targetImageView.getImage();
    if (currentImage != null) {
      currentImage.cancel();
    }

    Image image = new Image("file:///" + absoluteFilename, true);
    image.progressProperty().addListener((observable, oldValue, newValue) -> log.trace("Progress: " + newValue.doubleValue() * 100 + "%"));
    image.exceptionProperty().addListener((observable, oldValue, newValue) -> log.error(newValue.getMessage()));
    targetImageView.setImage(image);
  }

  private void showRemoteFile(SyncFile syncFile, ImageView targetImageView) {
    CopyCommand rcloneCopyService = new CopyCommand(Runtime.getRuntime(), syncFile);
    rcloneCopyService.setOnSucceeded(event -> {

      if (rcloneCopyService == latestCopyCommand) {
        targetImageView.setImage(rcloneCopyService.getLoadedImage());
        event.consume();
      }
    });
    rcloneCopyService.start();
    latestCopyCommand = rcloneCopyService;
  }

  @FXML
  public void diff() {
    model.getSourceOnly().clear();
    model.getContentDifferent().clear();
    model.getTargetOnly().clear();

    CheckCommand checkCommand = new CheckCommand(Runtime.getRuntime(), model);

    sourcePath.disableProperty().bind(checkCommand.runningProperty());
    targetPath.disableProperty().bind(checkCommand.runningProperty());
    sourceChooseButton.disableProperty().bind(checkCommand.runningProperty());
    targetChooseButton.disableProperty().bind(checkCommand.runningProperty());
    diffButton.disableProperty().bind(checkCommand.runningProperty());
    progressIndicator.visibleProperty().bind(checkCommand.runningProperty());

    checkCommand.setOnSucceeded(new CommandSucceededEvent(checkCommand, () -> {
      sourceOnlyLabel.setText("Local only (" + sourceOnly.getItems().size() + ")");
      diffsLabel.setText("Different content (" + diffs.getItems().size() + ")");
      targetOnlyLabel.setText("Remote only (" + targetOnly.getItems().size() + ")");
    }));
    checkCommand.start();
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

  /**
   * Deletes the selected file on the source side.
   */
  @FXML
  public void deleteSourceFile() {
    SyncFile syncFile = sourceOnly.getSelectionModel().selectedItemProperty().get();
    DeleteCommand deleteCommand = new DeleteCommand(Runtime.getRuntime(), sourcePath.getText() + "/" + syncFile.getFile());
    deleteCommand.setOnSucceeded(new CommandSucceededEvent(deleteCommand, () -> {
      model.getSourceOnly().remove(syncFile);
      sourceOnly.getSelectionModel().clearSelection();
    }));

    deleteCommand.start();
  }

  /**
   * Deletes the selected file on the target side.
   */
  @FXML
  public void deleteTargetFile() {
    SyncFile syncFile = targetOnly.getSelectionModel().selectedItemProperty().get();
    DeleteCommand deleteCommand = new DeleteCommand(Runtime.getRuntime(), targetPath.getText() + "/" + syncFile.getFile());
    deleteCommand.setOnSucceeded(new CommandSucceededEvent(deleteCommand, () -> {
      model.getTargetOnly().remove(syncFile);
      targetOnly.getSelectionModel().clearSelection();
    }));

    deleteCommand.start();
  }

  @FXML
  public void copyToTarget() {
    SyncFile syncFile = sourceOnly.getSelectionModel().selectedItemProperty().get();
    CopyCommand copyCommand = new CopyCommand(Runtime.getRuntime(), syncFile);
    copyCommand.setOnSucceeded(new CommandSucceededEvent(copyCommand, () -> {
      model.getSourceOnly().remove(syncFile);
      sourceOnly.getSelectionModel().clearSelection();
    }));
    copyCommand.start();
  }

  @FXML
  public void copyToSource() {
    SyncFile syncFile = targetOnly.getSelectionModel().selectedItemProperty().get();
    SyncFile syncFileInverse = new SyncFile(syncFile.getTargetPath(), syncFile.getSourcePath(), syncFile.getFile());
    CopyCommand copyCommand = new CopyCommand(Runtime.getRuntime(), syncFileInverse);
    copyCommand.setOnSucceeded(new CommandSucceededEvent(copyCommand, () -> {
      model.getTargetOnly().remove(syncFile);
      targetOnly.getSelectionModel().clearSelection();
    }));
    copyCommand.start();
  }

  @FXML
  public void showSourceImageLarge() {
    showImageLarge(sourceOnlyImage.getImage());
  }

  @FXML
  public void showTargetImageLarge() {
    showImageLarge(targetOnlyImage.getImage());
  }

  private void showImageLarge(Image image) {
    StackPane root = new StackPane();

    Stage stage = new Stage();
    stage.setTitle(image.getUrl());
    stage.setScene(new Scene(root, 800, 600));

    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.fitHeightProperty().bind(stage.heightProperty());
    imageView.fitWidthProperty().bind(stage.widthProperty());
    root.getChildren().add(imageView);

    stage.show();
  }
}
