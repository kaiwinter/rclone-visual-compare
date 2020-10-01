package com.github.kaiwinter.rclonediff.util;

/**
 * Utility class for String manipulations.
 */
public class StringUtils {

  private StringUtils() {
    // Utility class
  }

  /**
   * Wraps an String in Quotes. <code>string</code> becomes <code>"string"</code>.
   *
   * @param string
   *          the original String
   * @return the wrapped String
   */
  public static String wrapInQuotes(String string) {
    return "\"" + string + "\"";
  }

}
