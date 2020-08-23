package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;
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
  private static final String NOT_IN_LOCAL = ".*ERROR : (.*): File not in Local file system at \\/\\/\\?\\/{0}";
  private static final String NOT_IN_REMOTE = ".*ERROR : (.*): File not in .*'{0}'";
  private static final Pattern NUMBER_OF_DIFFERENCES = Pattern.compile(".* (.*) differences found");

  private final Runtime runtime;
  private final SyncEndpoint source;
  private final SyncEndpoint target;


  @Getter
  private List<SyncFile> sizeDiffer = new ArrayList<>();

  @Getter
  private List<SyncFile> notInSource = new ArrayList<>();

  @Getter
  private List<SyncFile> notInTarget = new ArrayList<>();

  @Override
  protected void execute() throws IOException {
    String command = "rclone check " + source.getPath().getValue() + " " + target.getPath().getValue();
    log.info("Check command: {}", command);

    Process process = runtime.exec(command);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    Pattern sourcePattern;
    Pattern targetPattern;

    if (source.getType() == Type.LOCAL) {
      sourcePattern = Pattern.compile(NOT_IN_LOCAL.replace("{0}", source.getPath().getValue()));
    } else if (source.getType() == Type.REMOTE) {
      sourcePattern = Pattern.compile(NOT_IN_REMOTE.replace("{0}", source.getPath().getValue()));
    } else {
      throw new IllegalArgumentException("Unknown type '" + source.getType() + "'");
    }

    if (target.getType() == Type.LOCAL) {
      targetPattern = Pattern.compile(NOT_IN_LOCAL.replace("{0}", target.getPath().getValue()));
    } else if (target.getType() == Type.REMOTE) {
      targetPattern = Pattern.compile(NOT_IN_REMOTE.replace("{0}", target.getPath().getValue()));
    } else {
      throw new IllegalArgumentException("Unknown type '" + target.getType() + "'");
    }

    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher;


      if ((matcher = SIZES_DIFFER.matcher(line)).matches()) {
        sizeDiffer.add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), matcher.group(1)));
        log.info(line + " (differences)");

      } else if ((matcher = sourcePattern.matcher(line)).matches()) {
        notInSource.add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), matcher.group(1)));
        log.info(line + " (missing in source)");

      } else if ((matcher = targetPattern.matcher(line)).matches()) {
        notInTarget.add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), matcher.group(1)));
        log.info(line + " (missing in target)");

      } else if ((matcher = NUMBER_OF_DIFFERENCES.matcher(line)).matches()) {
        log.info(line + " (summary check)");
        long expectedEntries = Long.valueOf(matcher.group(1));
        long actualEntries = sizeDiffer.size() + notInSource.size() + notInTarget.size();
        if (expectedEntries != actualEntries) {
          String message = "Excepted " + expectedEntries + " parsed differences, actually parsed: " + actualEntries;
          log.error(message);
          throw new AssertionError(message);
        }
      } else {
        log.info(line + " (unrecognized)");
      }
    }

    wait(process);
    log.debug("check value code {}", process.exitValue());
  }
}
