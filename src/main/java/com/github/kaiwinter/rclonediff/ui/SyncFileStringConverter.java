package com.github.kaiwinter.rclonediff.ui;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.util.StringConverter;

/**
 * Renders an implementation of {@link SyncFile}. The opposite direction is not implemented!
 *
 * @param <T>
 *          Implementation of {@link SyncFile}
 */
public class SyncFileStringConverter<T extends SyncFile> extends StringConverter<T> {

  @Override
  public String toString(T object) {
    return object.toUiString();
  }

  @Override
  public T fromString(String string) {
    throw new UnsupportedOperationException();
  }

}
