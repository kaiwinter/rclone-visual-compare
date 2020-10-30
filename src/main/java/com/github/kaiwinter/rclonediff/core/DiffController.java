package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.command.CheckCommand;
import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.command.DeleteCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineService;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * The controller class of the view RcloneDiff.
 */
@Slf4j
public class DiffController implements Initializable {

  @FXML
  private TextField sourcePath;

  @FXML
  private TextField targetPath;

  @FXML
  private ListView<SyncFile> sourceOnly;

  @FXML
  private ListView<SyncFile> diffs;

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

  @FXML
  private Button sourceChooseButton;

  @FXML
  private Button targetChooseButton;

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
  private Button copyToTargetFromDiffButton;

  @FXML
  private Button copyToSourceFromDiffButton;

  @FXML
  private ProgressIndicator progressIndicator;

  private Path tempDirectory;

  private DiffModel model = new DiffModel();

  private RcloneCommandlineServiceFactory serviceFactory = new RcloneCommandlineServiceFactory(Runtime.getRuntime());

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

    copyToTargetFromDiffButton.disableProperty().bind(Bindings.isEmpty(diffs.getSelectionModel().getSelectedItems()));
    copyToSourceFromDiffButton.disableProperty().bind(Bindings.isEmpty(diffs.getSelectionModel().getSelectedItems()));

    StringConverter<SyncEndpoint> converter = new StringConverter<>() {

      @Override
      public String toString(SyncEndpoint object) {
        if (object == null) {
          return null;
        }
        return object.toUiString();
      }

      @Override
      public SyncEndpoint fromString(String string) {
        throw new UnsupportedOperationException("Converting from String to SyncEndpoint not implemented");
      }

    };
    Bindings.bindBidirectional(sourcePath.textProperty(), model.getSource(), converter);
    Bindings.bindBidirectional(targetPath.textProperty(), model.getTarget(), converter);

    sourceOnly.setItems(model.getSourceOnly());
    diffs.setItems(model.getContentDifferent());
    targetOnly.setItems(model.getTargetOnly());

    PreferencesStore.loadSourceEndpoint().ifPresent(syncEndpoint -> model.getSource().setValue(syncEndpoint));
    PreferencesStore.loadTargetEndpoint().ifPresent(syncEndpoint -> model.getTarget().setValue(syncEndpoint));
    PreferencesStore.loadRcloneBinaryPath().ifPresent(rcloneBinaryPath -> model.getRcloneBinaryPath().setValue(rcloneBinaryPath));
  }

  private void showImageFromSourcePath(SyncFile syncFile) {
    sourceOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    String path = syncFile.getSourcePath();

    if (isLocalPath(path)) {
      Path completeFilePath = Path.of(syncFile.getSourcePath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, sourceOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getSourcePath(), getTempDirectoryLazy().toString(), syncFile.getFile()), sourceOnlyImage);
    }
  }

  private void showImageFromTargetPath(SyncFile syncFile) {
    targetOnlyImage.setImage(null);
    if (syncFile == null) {
      return;
    }
    if (!isImage(syncFile)) {
      return;
    }

    String path = syncFile.getTargetPath();

    if (isLocalPath(path)) {
      Path completeFilePath = Path.of(syncFile.getTargetPath()).resolve(syncFile.getFile());
      showLocalFile(completeFilePath, targetOnlyImage);
    } else {
      showRemoteFile(new SyncFile(syncFile.getTargetPath(), getTempDirectoryLazy().toString(), syncFile.getFile()), targetOnlyImage);
    }
  }

  private boolean isImage(SyncFile syncFile) {
    String fileExtension = syncFile.getFile().toLowerCase();
    return fileExtension.endsWith(".jpg") //
      || fileExtension.endsWith(".jpeg") //
      || fileExtension.endsWith(".png") //
      || fileExtension.endsWith(".gif") //
      || fileExtension.endsWith(".bmp");
  }

  private boolean isLocalPath(String path) {
    try {
      Paths.get(path);
    } catch (InvalidPathException | NullPointerException ex) {
      return false;
    }
    return true;
  }

  private void showLocalFile(Path absoluteFilename, ImageView targetImageView) {
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
    Path completeFilePath = Path.of(syncFile.getTargetPath()).resolve(syncFile.getFile());
    if (completeFilePath.toFile().exists()) {
      showLocalFile(completeFilePath, targetImageView);
      return;
    }
    CopyCommand rcloneCopyService = new CopyCommand(syncFile);
    rcloneCopyService.setCommandSucceededEvent(() -> {

      if (rcloneCopyService == model.getLatestCopyCommand()) {
        showLocalFile(completeFilePath, targetImageView);
      }
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), rcloneCopyService);
    model.setLatestCopyCommand(rcloneCopyService);
  }

  /**
   * Called to start a rclone check of the two selected paths.
   */
  @FXML
  public void diff() {
    // re-use diff button as cancel button
    if ("Cancel".equals(diffButton.getText())) {
      model.getRunningCheckCommand().cancel();
      diffButton.setText("Diff");
      return;
    }
    diffButton.setText("Cancel");
    model.getSourceOnly().clear();
    model.getContentDifferent().clear();
    model.getTargetOnly().clear();

    CheckCommand checkCommand = new CheckCommand(model);
    RcloneCommandlineService checkService = serviceFactory.createService(model.getRcloneBinaryPath().getValue(), checkCommand);
    model.setRunningCheckCommand(checkService);

    sourcePath.disableProperty().bind(checkService.runningProperty());
    targetPath.disableProperty().bind(checkService.runningProperty());
    sourceChooseButton.disableProperty().bind(checkService.runningProperty());
    targetChooseButton.disableProperty().bind(checkService.runningProperty());
    progressIndicator.visibleProperty().bind(checkService.runningProperty());

    checkCommand.setCommandSucceededEvent(() -> {
      sourceOnlyLabel.setText("Source only (" + sourceOnly.getItems().size() + ")");
      diffsLabel.setText("Different content (" + diffs.getItems().size() + ")");
      targetOnlyLabel.setText("Target only (" + targetOnly.getItems().size() + ")");
      diffButton.setText("Diff");
    });
    checkService.start();
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
    boolean delete = model.isAlwaysDelete();
    if (!delete) {
      delete = askForDeleteConfirmation(syncFile.getFile());
    }

    if (delete) {
      DeleteCommand deleteCommand = new DeleteCommand(syncFile.getSourcePath() + syncFile.getFile());
      deleteCommand.setCommandSucceededEvent(() -> {
        model.getSourceOnly().remove(syncFile);
      });

      serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), deleteCommand);
    }
  }

  /**
   * Deletes the selected file on the target side.
   */
  @FXML
  public void deleteTargetFile() {
    SyncFile syncFile = targetOnly.getSelectionModel().selectedItemProperty().get();
    boolean delete = model.isAlwaysDelete();
    if (!delete) {
      delete = askForDeleteConfirmation(syncFile.getFile());
    }

    if (delete) {
      DeleteCommand deleteCommand = new DeleteCommand(syncFile.getTargetPath() + syncFile.getFile());
      deleteCommand.setCommandSucceededEvent(() -> {
        model.getTargetOnly().remove(syncFile);
      });

      serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), deleteCommand);
    }
  }

  private boolean askForDeleteConfirmation(String filename) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Delete confirmation");
    alert.setHeaderText("Do you really want to delete '" + filename + "'");
    alert.setContentText("Please confirm the deletion of the file. You can choose 'Yes, always' to suppress any further confirmations.");

    ButtonType buttonYes = new ButtonType("Yes");
    ButtonType buttonYesAlways = new ButtonType("Yes, always");
    ButtonType buttonNo = new ButtonType("No, cancel", ButtonData.CANCEL_CLOSE);

    alert.getButtonTypes().setAll(buttonYes, buttonYesAlways, buttonNo);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == buttonYes) {
      return true;
    } else if (result.get() == buttonYesAlways) {
      model.setAlwaysDelete(true);
      return true;
    }
    return false;
  }

  /**
   * Called to copy a file selected on the source side to the target side.
   */
  @FXML
  public void copyToTarget() {
    SyncFile syncFile = sourceOnly.getSelectionModel().selectedItemProperty().get();
    CopyCommand copyCommand = new CopyCommand(syncFile);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getSourceOnly().remove(syncFile);
    });
    serviceFactory.createService(model.getRcloneBinaryPath().getValue(), copyCommand).start();
  }

  /**
   * Called to copy a file selected on the target side to the source side.
   */
  @FXML
  public void copyToSource() {
    SyncFile syncFile = targetOnly.getSelectionModel().selectedItemProperty().get();
    SyncFile syncFileInverse = new SyncFile(syncFile.getTargetPath(), syncFile.getSourcePath(), syncFile.getFile());
    CopyCommand copyCommand = new CopyCommand(syncFileInverse);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getTargetOnly().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to copy a file selected in the "different content" list to the target side.
   */
  @FXML
  public void copyToTargetFromDiff() {
    SyncFile syncFile = diffs.getSelectionModel().selectedItemProperty().get();
    CopyCommand copyCommand = new CopyCommand(syncFile);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getContentDifferent().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to copy a file selected in the "different content" list to the source side.
   */
  @FXML
  public void copyToSourceFromDiff() {
    SyncFile syncFile = diffs.getSelectionModel().selectedItemProperty().get();
    SyncFile syncFileInverse = new SyncFile(syncFile.getTargetPath(), syncFile.getSourcePath(), syncFile.getFile());
    CopyCommand copyCommand = new CopyCommand(syncFileInverse);
    copyCommand.setCommandSucceededEvent(() -> {
      model.getContentDifferent().remove(syncFile);
    });
    serviceFactory.createServiceAndStart(model.getRcloneBinaryPath().getValue(), copyCommand);
  }

  /**
   * Called to show the selected image from the source side.
   */
  @FXML
  public void showSourceImageLarge() {
    showImageLarge(sourceOnlyImage.getImage());
  }

  /**
   * Called to show the selected image from the target side.
   */
  @FXML
  public void showTargetImageLarge() {
    showImageLarge(targetOnlyImage.getImage());
  }

  private void showImageLarge(Image image) {
    if (image == null) {
      return;
    }
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

  /**
   * Called to open the dialog to choose the source path.
   */
  @FXML
  public void chooseSourcePath() {
    PathDialogController pathDialogController = new PathDialogController(model.getSource().getValue());
    Optional<SyncEndpoint> result = pathDialogController.showAndWait();
    result.ifPresent(syncEndpoint -> {
      model.getSource().setValue(syncEndpoint);
      PreferencesStore.saveSourceEndpoint(syncEndpoint);
    });
  }

  /**
   * Called to open the dialog to choose the target path.
   */
  @FXML
  public void chooseTargetPath() {
    PathDialogController pathDialogController = new PathDialogController(model.getTarget().getValue());
    Optional<SyncEndpoint> result = pathDialogController.showAndWait();
    result.ifPresent(syncEndpoint -> {
      model.getTarget().setValue(syncEndpoint);
      PreferencesStore.saveTargetEndpoint(syncEndpoint);
    });
  }

  /**
   * Called to open the preferences dialog.
   */
  @FXML
  public void openPreferences() {
    TextInputDialog dialog = new TextInputDialog(model.getRcloneBinaryPath().getValue());
    dialog.setTitle("rclone binary path");
    dialog.setHeaderText("rclone binary path");
    dialog.setContentText("rclone binary path:");

    // disable ok button if input is empty
    BooleanBinding isValid = Bindings.createBooleanBinding(() -> dialog.getEditor().getText().isBlank(), dialog.getEditor().textProperty());
    Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(isValid);

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(rcloneBinaryPath -> {
      model.getRcloneBinaryPath().setValue(rcloneBinaryPath);
      PreferencesStore.saveRcloneBinaryPath(rcloneBinaryPath);
    });
  }
}
