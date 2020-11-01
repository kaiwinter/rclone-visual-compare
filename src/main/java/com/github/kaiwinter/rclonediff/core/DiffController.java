package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

import com.github.kaiwinter.rclonediff.command.CheckCommand;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineService;
import com.github.kaiwinter.rclonediff.command.RcloneCommandlineServiceFactory;
import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncFile;
import com.github.kaiwinter.rclonediff.service.DiffService;
import com.github.kaiwinter.rclonediff.ui.SyncFileStringConverter;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
  private DiffService diffService = new DiffService(serviceFactory);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    sourceOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));
    diffs.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));
    targetOnly.setCellFactory(TextFieldListCell.forListView(new SyncFileStringConverter()));

    diffs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      diffService.showImageFromSourcePath(newValue, model.sourceImageProperty(), model);
      diffService.showImageFromTargetPath(newValue, model.targetImageProperty(), model);
    });

    sourceOnly.getSelectionModel().selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> diffService.showImageFromSourcePath(newValue, model.sourceImageProperty(), model));
    targetOnly.getSelectionModel().selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> diffService.showImageFromTargetPath(newValue, model.targetImageProperty(), model));

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

    model.selectedSourceFileProperty().bind(sourceOnly.getSelectionModel().selectedItemProperty());
    model.selectedTargetFileProperty().bind(targetOnly.getSelectionModel().selectedItemProperty());
    model.selectedDiffFileProperty().bind(diffs.getSelectionModel().selectedItemProperty());

    Bindings.bindBidirectional(sourceOnlyImage.imageProperty(), model.sourceImageProperty());
    Bindings.bindBidirectional(targetOnlyImage.imageProperty(), model.targetImageProperty());

    sourceOnly.setItems(model.getSourceOnly());
    diffs.setItems(model.getContentDifferent());
    targetOnly.setItems(model.getTargetOnly());

    PreferencesStore.loadSourceEndpoint().ifPresent(syncEndpoint -> model.getSource().setValue(syncEndpoint));
    PreferencesStore.loadTargetEndpoint().ifPresent(syncEndpoint -> model.getTarget().setValue(syncEndpoint));
    PreferencesStore.loadRcloneBinaryPath().ifPresent(rcloneBinaryPath -> model.getRcloneBinaryPath().setValue(rcloneBinaryPath));
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
    diffService.deleteSourceFile(model);
  }

  /**
   * Deletes the selected file on the target side.
   */
  @FXML
  public void deleteTargetFile() {
    diffService.deleteTargetFile(model);
  }

  /**
   * Called to copy a file selected on the source side to the target side.
   */
  @FXML
  public void copyToTarget() {
    diffService.copyToTarget(model);
  }

  /**
   * Called to copy a file selected on the target side to the source side.
   */
  @FXML
  public void copyToSource() {
    diffService.copyToSource(model);
  }

  /**
   * Called to copy a file selected in the "different content" list to the target side.
   */
  @FXML
  public void copyToTargetFromDiff() {
    diffService.copyToTargetFromDiff(model);
  }

  /**
   * Called to copy a file selected in the "different content" list to the source side.
   */
  @FXML
  public void copyToSourceFromDiff() {
    diffService.copyToSourceFromDiff(model);
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
