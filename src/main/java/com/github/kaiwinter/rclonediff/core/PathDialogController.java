package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.util.Optional;

import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * Controller of the path selection dialog. The Dialog is initialized and open by calling its
 * constructor.
 */
public class PathDialogController {

  @FXML
  private TextField path;

  @FXML
  private ToggleGroup type;

  @FXML
  private Toggle localToggle;

  @FXML
  private Toggle remoteToggle;

  @FXML
  private Label slashesInfo;

  private Dialog<SyncEndpoint> dialog;

  /**
   * Constructs a new dialog to let the user select a {@link SyncEndpoint}.
   *
   * @param syncEndpoint
   *          the current {@link SyncEndpoint} to be shown in the dialog
   */
  public PathDialogController(SyncEndpoint syncEndpoint) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/PathDialog.fxml"));
    fxmlLoader.setController(this);

    try {
      DialogPane dialogPane = fxmlLoader.load();

      if (syncEndpoint != null) {
        showCurrentSyncEndpoint(syncEndpoint);
      }

      dialog = new Dialog<>();
      dialog.setTitle("Configure path");

      BooleanBinding isPathValid = Bindings.createBooleanBinding(() -> path.getText().isBlank(), path.textProperty());
      BooleanBinding isTypeValid = Bindings.createBooleanBinding(() -> type.getSelectedToggle() == null, type.selectedToggleProperty());
      BooleanBinding correctSlashes = Bindings.createBooleanBinding(() -> path.getText().contains("\\"), path.textProperty());

      slashesInfo.visibleProperty().bind(correctSlashes);

      Node okButton = dialogPane.lookupButton(ButtonType.OK);
      okButton.disableProperty().bind(isPathValid.or(isTypeValid).or(correctSlashes));

      dialog.setResultConverter(buttonType -> {

        if (buttonType == ButtonType.CANCEL) {
          return null;
        }

        Type syncEndpointType;
        if (type.getSelectedToggle() == localToggle) {
          syncEndpointType = Type.LOCAL;
        } else if (type.getSelectedToggle() == remoteToggle) {
          syncEndpointType = Type.REMOTE;
        } else {
          throw new IllegalArgumentException("Unknown type");
        }
        return new SyncEndpoint(syncEndpointType, path.getText().trim());
      });
      dialog.setDialogPane(dialogPane);
      Platform.runLater(() -> path.requestFocus());
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private void showCurrentSyncEndpoint(SyncEndpoint syncEndpoint) {
    this.path.setText(syncEndpoint.getPath());
    if (syncEndpoint.getType() == Type.LOCAL) {
      this.localToggle.setSelected(true);
    } else if (syncEndpoint.getType() == Type.REMOTE) {
      this.remoteToggle.setSelected(true);
    }
  }

  /**
   * Shows the dialog and returns the result when the user confirms the dialog
   *
   * @return a valid {@link SyncEndpoint} or an empty optional
   */
  public Optional<SyncEndpoint> showAndWait() {
    return dialog.showAndWait();
  }
}
