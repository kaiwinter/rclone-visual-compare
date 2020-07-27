package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.model.SyncFile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone check command.
 */
@Slf4j
@RequiredArgsConstructor
public class CheckCommand extends AbstractCommand {

  private static final Pattern SIZES_DIFFER = Pattern.compile(".*ERROR : (.*): Sizes differ");
  private static final Pattern NOT_IN_LOCAL = Pattern.compile(".*ERROR : (.*): File not in Local file system at \\/\\/\\?\\/(.*)");
  private static final Pattern NOT_IN_REMOTE = Pattern.compile(".*ERROR : (.*): File not in .*'(.*)'");
  private static final Pattern NUMBER_OF_DIFFERENCES = Pattern.compile(".* (.*) differences found");

  private final Runtime runtime;
  private final String localPath;
  private final String remotePath;

  @Getter
  private List<SyncFile> sizeDiffer = new ArrayList<>();

  @Getter
  private List<SyncFile> notInLocal = new ArrayList<>();

  @Getter
  private List<SyncFile> notInRemote = new ArrayList<>();

  @Override
  protected void execute() throws IOException {
    String command = "rclone check " + localPath + " " + remotePath;
    log.info("Check command: {}", command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher;
      log.debug(line);

      if ((matcher = SIZES_DIFFER.matcher(line)).matches()) {
        sizeDiffer.add(new SyncFile(matcher.group(1), localPath, remotePath));
      } else if ((matcher = NOT_IN_LOCAL.matcher(line)).matches()) {
        notInLocal.add(new SyncFile(matcher.group(1), matcher.group(2), remotePath));
      } else if ((matcher = NOT_IN_REMOTE.matcher(line)).matches()) {
        notInRemote.add(new SyncFile(matcher.group(1), localPath, matcher.group(2)));
      } else if ((matcher = NUMBER_OF_DIFFERENCES.matcher(line)).matches()) {
        long expectedEntries = Long.valueOf(matcher.group(1));
        long actualEntries = sizeDiffer.size() + notInLocal.size() + notInRemote.size();
        if (expectedEntries != actualEntries) {
          String message = "Excepted " + expectedEntries + " parsed differences, actually parsed: " + actualEntries;
          log.error(message);
          throw new AssertionError(message);
        }
      }
    }

    wait(process);
    log.debug("check value code {}", process.exitValue());
  }
}