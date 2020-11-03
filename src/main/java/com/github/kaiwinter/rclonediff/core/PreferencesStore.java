package com.github.kaiwinter.rclonediff.core;

import java.util.Optional;
import java.util.prefs.Preferences;

import com.github.kaiwinter.rclonediff.MainApplication;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;

/**
 * The Application stores preferences. This class provides access to them.
 */
public class PreferencesStore {

  private static final String SOURCE_PATH = "source.path";
  private static final String SOURCE_PATH_TYPE = "source.path.type";
  private static final String TARGET_PATH = "target.path";
  private static final String TARGET_PATH_TYPE = "target.path.type";
  private static final String RCLONE_BINARY_PATH = "rclone.binary.path";

  /**
   * Saves a {@link SyncEndpoint} as source endpoint for later use.
   *
   * @param syncEndpoint
   *          {@link SyncEndpoint} to save
   */
  public static void saveSourceEndpoint(SyncEndpoint syncEndpoint) {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    pref.put(SOURCE_PATH_TYPE, syncEndpoint.getType().name());
    pref.put(SOURCE_PATH, syncEndpoint.getPath());
  }

  /**
   * Saves a {@link SyncEndpoint} as target endpoint for later use.
   *
   * @param syncEndpoint
   *          {@link SyncEndpoint} to save
   */
  public static void saveTargetEndpoint(SyncEndpoint syncEndpoint) {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    pref.put(TARGET_PATH_TYPE, syncEndpoint.getType().name());
    pref.put(TARGET_PATH, syncEndpoint.getPath());
  }

  /**
   * Loads a previously saved source {@link SyncEndpoint}.
   *
   * @return source {@link SyncEndpoint} or empty Optional
   */
  public static Optional<SyncEndpoint> loadSourceEndpoint() {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    String sourcePathTypeFromPreferences = pref.get(SOURCE_PATH_TYPE, null);
    String sourcePathFromPreferences = pref.get(SOURCE_PATH, null);
    if (sourcePathFromPreferences != null && sourcePathTypeFromPreferences != null) {
      return Optional.of(new SyncEndpoint(Type.valueOf(sourcePathTypeFromPreferences), sourcePathFromPreferences));
    }
    return Optional.empty();
  }

  /**
   * Loads a previously saved target {@link SyncEndpoint}.
   *
   * @return target {@link SyncEndpoint} or empty Optional
   */
  public static Optional<SyncEndpoint> loadTargetEndpoint() {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    String targetPathTypeFromPreferences = pref.get(TARGET_PATH_TYPE, null);
    String targetPathFromPreferences = pref.get(TARGET_PATH, null);
    if (targetPathFromPreferences != null && targetPathTypeFromPreferences != null) {
      return Optional.of(new SyncEndpoint(Type.valueOf(targetPathTypeFromPreferences), targetPathFromPreferences));
    }
    return Optional.empty();
  }

  /**
   * Loads the path to the rclone binary. If it is not set, "rclone" is returned as default.
   *
   * @return the path to the rclone binary, or "rclone" if not set.
   */
  public static String loadRcloneBinaryPath() {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    return pref.get(RCLONE_BINARY_PATH, "rclone");
  }

  /**
   * Saves the path to the rclone executable.
   *
   * @param rcloneBinaryPath
   *          the path to the rclone executable
   */
  public static void saveRcloneBinaryPath(String rcloneBinaryPath) {
    Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
    pref.put(RCLONE_BINARY_PATH, rcloneBinaryPath);
  }
}
