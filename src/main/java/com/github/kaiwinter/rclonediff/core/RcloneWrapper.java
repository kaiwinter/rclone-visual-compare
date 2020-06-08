package com.github.kaiwinter.rclonediff.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RcloneWrapper {

  private static final Pattern SIZES_DIFFER = Pattern.compile(".*ERROR : (.*): Sizes differ");
  private static final Pattern NOT_IN_LOCAL = Pattern.compile(".*ERROR : (.*): File not in Local file system at \\/\\/\\?\\/(.*)");
  private static final Pattern NOT_IN_REMOTE = Pattern.compile(".*ERROR : (.*): File not in .*'(.*)'");
  private static final Pattern NUMBER_OF_DIFFERENCES = Pattern.compile(".* (.*) differences found");

  @Getter
  private List<String> sizeDiffer = new ArrayList<>();

  @Getter
  private List<RemoteOnlyFile> notInLocal = new ArrayList<>();

  @Getter
  private List<LocalOnlyFile> notInRemote = new ArrayList<>();

  public void check(String localPath, String remotePath) throws IOException {
    String command = "rclone check " + localPath + " " + remotePath;
    log.info("Check command: {}", command);

    Process process = Runtime.getRuntime().exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher;
      log.debug(line);

      if ((matcher = SIZES_DIFFER.matcher(line)).matches()) {
        sizeDiffer.add(matcher.group(1));
      } else if ((matcher = NOT_IN_LOCAL.matcher(line)).matches()) {
        notInLocal.add(new RemoteOnlyFile(matcher.group(1), matcher.group(2), remotePath));
      } else if ((matcher = NOT_IN_REMOTE.matcher(line)).matches()) {
        notInRemote.add(new LocalOnlyFile(matcher.group(1), localPath, matcher.group(2)));
      } else if ((matcher = NUMBER_OF_DIFFERENCES.matcher(line)).matches()) {
        long expectedEntries = Long.valueOf(matcher.group(1));
        long actualEntries = sizeDiffer.size() + notInLocal.size() + notInRemote.size();
        if (expectedEntries != actualEntries) {
          throw new AssertionError("Excepted " + expectedEntries + " actually: " + actualEntries);
        }
      }
    }

    wait(process);
    log.debug("check value code {}", process.exitValue());
  }

  public static void copy(String file, String fromPath, String toPath) throws IOException {
    String command = "rclone copy " + fromPath + "/" + file + " " + toPath;
    log.info("Copy command: {}", command);

    Process process = Runtime.getRuntime().exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      log.error(line);
    }
    wait(process);
    log.info("check value code {}", process.exitValue());
  }

  private static void wait(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
