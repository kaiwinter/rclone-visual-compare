package com.github.kaiwinter.rclonediff.core;

import java.io.IOException;
import java.util.Optional;

import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import lombok.Getter;

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

  @Getter
  private Optional<SyncEndpoint> result;

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

      Dialog<SyncEndpoint> dialog = new Dialog<>();
      dialog.setTitle("Configure path");
      dialog.setResultConverter(buttonType -> {

        if (buttonType == ButtonType.CANCEL) {
          return null;
        }
        // TODO KW: Validate valid input. Is there a way to allow OK only if path is set and type is
        // selected?
        Type syncEndpointType;
        if (type.getSelectedToggle() == localToggle) {
          syncEndpointType = Type.LOCAL;
        } else if (type.getSelectedToggle() == remoteToggle) {
          syncEndpointType = Type.REMOTE;
        } else {
          throw new IllegalArgumentException("No Type selected");
        }
        return new SyncEndpoint(syncEndpointType, path.getText().trim());
      });
      dialog.setDialogPane(dialogPane);
      Platform.runLater(() -> path.requestFocus());
      result = dialog.showAndWait();
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
}
