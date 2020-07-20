package com.github.kaiwinter.rclonediff.ui;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.util.StringConverter;

/**
 * Renders a {@link SyncFile}. The opposite direction is not implemented!
 */
public class SyncFileStringConverter extends StringConverter<SyncFile> {

  @Override
  public String toString(SyncFile object) {
    return object.toUiString();
  }

  @Override
  public SyncFile fromString(String string) {
    throw new UnsupportedOperationException();
  }

}
