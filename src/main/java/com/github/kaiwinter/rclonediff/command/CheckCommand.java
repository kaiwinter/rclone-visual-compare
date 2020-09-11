package com.github.kaiwinter.rclonediff.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.core.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;
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

  private final Runtime runtime;
  private final DiffModel model;

  private boolean isCancelled = false;
  private Process process;

  @Override
  protected void execute() throws IOException {
    SyncEndpoint source = model.getSource();
    SyncEndpoint target = model.getTarget();

    String command = "rclone check " + source.getPath().getValue() + " " + target.getPath().getValue();
    log.info("Check command: {}", command);
    consoleLog.add(command);

    process = runtime.exec(command);
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
      if (isCancelled) {
        break;
      }
      consoleLog.add(line);
      Matcher matcher;

      if ((matcher = SIZES_DIFFER.matcher(line)).matches()) {
        final Matcher m = matcher;
        Platform.runLater(
          () -> model.getContentDifferent().add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), m.group(1))));
        log.info(line + " (differences)");

      } else if ((matcher = sourcePattern.matcher(line)).matches()) {

        final Matcher m = matcher;
        Platform
          .runLater(() -> model.getTargetOnly().add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), m.group(1))));
        log.info(line + " (missing in source)");

      } else if ((matcher = targetPattern.matcher(line)).matches()) {

        final Matcher m = matcher;
        Platform
          .runLater(() -> model.getSourceOnly().add(new SyncFile(source.getPath().getValue(), target.getPath().getValue(), m.group(1))));
        log.info(line + " (missing in target)");

      } else {
        log.info(line + " (unrecognized)");
      }
    }

    wait(process);
    returnCode = process.exitValue();
    log.info("rclone return code: {}", returnCode);
  }

  @Override
  protected void cancelled() {
    this.isCancelled = true;
    this.process.destroy();
  }

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0, // source and target equal
        1 // source and target not equal
    };
  }
}
