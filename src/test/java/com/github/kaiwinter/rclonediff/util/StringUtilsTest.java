package com.github.kaiwinter.rclonediff.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringUtils}.
 */
class StringUtilsTest {

  @Test
  void wrapInQuotes() {
    assertEquals("\"string\"", StringUtils.wrapInQuotes("string"));
  }

}
