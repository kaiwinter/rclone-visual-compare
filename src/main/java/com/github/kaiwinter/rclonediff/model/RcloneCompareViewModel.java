package com.github.kaiwinter.rclonediff.model;

import com.github.kaiwinter.rclonediff.command.CopyCommand;
import com.github.kaiwinter.rclonediff.view.RcloneCompareView;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.image.Image;
import lombok.Data;

/**
 * ViewModel for the {@link RcloneCompareView}.
 */
@Data
public class RcloneCompareViewModel {
  private ObjectProperty<SyncEndpoint> source = new SimpleObjectProperty<>();
  private ObjectProperty<SyncEndpoint> target = new SimpleObjectProperty<>();

  /** Files which are only on the source side only. */
  private ObservableList<SyncFile> sourceOnly = FXCollections.observableArrayList();

  /** Files which are different on source and target side. */
  private ObservableList<SyncFile> contentDifferent = FXCollections.observableArrayList();

  /** Files which are on the target side only. */
  private ObservableList<SyncFile> targetOnly = FXCollections.observableArrayList();

  private Service<Void> runningCheckCommand;

  private CopyCommand latestCopyCommand;

  /** If false, the user gets asked before any delete operation. */
  private boolean alwaysDelete = false;

  private ObservableList<SyncFile> selectedSourceFiles;
  private ObservableList<SyncFile> selectedDiffFiles;
  private ObservableList<SyncFile> selectedTargetFiles;

  private final ObjectProperty<Image> sourceImage = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> targetImage = new SimpleObjectProperty<>();



  public ObjectProperty<Image> targetImageProperty() {
    return this.targetImage;
  }


  public Image getTargetImage() {
    return this.targetImageProperty().get();
  }


  public void setTargetImage(final Image targetImage) {
    this.targetImageProperty().set(targetImage);
  }

  public ObjectProperty<Image> sourceImageProperty() {
    return this.sourceImage;
  }


  public Image getSourceImage() {
    return this.sourceImageProperty().get();
  }


  public void setSourceImage(final Image sourceImage) {
    this.sourceImageProperty().set(sourceImage);
  }

}
