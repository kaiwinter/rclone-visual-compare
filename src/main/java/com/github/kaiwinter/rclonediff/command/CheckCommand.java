package com.github.kaiwinter.rclonediff.command;

import static com.github.kaiwinter.rclonediff.util.StringUtils.wrapInQuotes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.model.RcloneCompareViewModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;
import com.github.kaiwinter.rclonediff.model.SyncFile;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes a rclone check command.
 */
@Slf4j
public class CheckCommand extends AbstractCommand {

  private static final Pattern SIZES_DIFFER = Pattern.compile(".*ERROR : (.*): Sizes differ", Pattern.CASE_INSENSITIVE);
  private static final String NOT_IN_LOCAL = ".*ERROR : (.*): File not in Local file system at \\/\\/\\?\\/{0}";
  private static final String NOT_IN_REMOTE = ".*ERROR : (.*): File not in .*'{0}'";

  private final RcloneCompareViewModel model;

  private final Pattern sourcePattern;
  private final Pattern targetPattern;

  /**
   * Constructs a new {@link CheckCommand} and initializes regular expressions to parse rclone output.
   *
   * @param model
   *          the {@link RcloneCompareViewModel}
   */
  public CheckCommand(RcloneCompareViewModel model) {
    this.model = model;

    SyncEndpoint source = model.getSource().getValue();
    SyncEndpoint target = model.getTarget().getValue();

    if (source.getType() == Type.LOCAL) {
      sourcePattern = Pattern.compile(NOT_IN_LOCAL.replace("{0}", Pattern.quote(source.getPath())), Pattern.CASE_INSENSITIVE);
    } else if (source.getType() == Type.REMOTE) {
      sourcePattern = Pattern.compile(NOT_IN_REMOTE.replace("{0}", Pattern.quote(source.getPath())), Pattern.CASE_INSENSITIVE);
    } else {
      throw new IllegalArgumentException("Unknown type '" + source.getType() + "'");
    }

    if (target.getType() == Type.LOCAL) {
      targetPattern = Pattern.compile(NOT_IN_LOCAL.replace("{0}", Pattern.quote(target.getPath())), Pattern.CASE_INSENSITIVE);
    } else if (target.getType() == Type.REMOTE) {
      targetPattern = Pattern.compile(NOT_IN_REMOTE.replace("{0}", Pattern.quote(target.getPath())), Pattern.CASE_INSENSITIVE);
    } else {
      throw new IllegalArgumentException("Unknown type '" + target.getType() + "'");
    }
  }

  @Override
  public int[] getExpectedReturnCodes() {
    return new int[] {0, // source and target equal
        1 // source and target not equal
    };
  }

  @Override
  public String getCommandline() {
    SyncEndpoint source = model.getSource().getValue();
    SyncEndpoint target = model.getTarget().getValue();

    return "check " + wrapInQuotes(source.getPath()) + " " + wrapInQuotes(target.getPath());
  }

  @Override
  protected void handleRcloneOutput(String line) {
    SyncEndpoint source = model.getSource().getValue();
    SyncEndpoint target = model.getTarget().getValue();

    Matcher matcher;

    if ((matcher = SIZES_DIFFER.matcher(line)).matches()) {
      final Matcher m = matcher;
      Platform.runLater(() -> model.getContentDifferent().add(new SyncFile(source, target, m.group(1))));
      log.info(line + " (differences)");

    } else if ((matcher = sourcePattern.matcher(line)).matches()) {

      final Matcher m = matcher;
      Platform.runLater(() -> model.getTargetOnly().add(new SyncFile(source, target, m.group(1))));
      log.info(line + " (missing in source)");

    } else if ((matcher = targetPattern.matcher(line)).matches()) {

      final Matcher m = matcher;
      Platform.runLater(() -> model.getSourceOnly().add(new SyncFile(source, target, m.group(1))));
      log.info(line + " (missing in target)");

    } else {
      log.info(line + " (unrecognized)");
    }
  }
}
