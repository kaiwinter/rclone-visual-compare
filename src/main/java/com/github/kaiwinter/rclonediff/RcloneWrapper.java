package com.github.kaiwinter.rclonediff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kaiwinter.rclonediff.model.DirectoryEntry;
import com.github.kaiwinter.rclonediff.model.LocalOnlyFile;
import com.github.kaiwinter.rclonediff.model.RemoteOnlyFile;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;

public class RcloneWrapper {

  public static void main(String[] args) throws InterruptedException, IOException {
    new RcloneWrapper().check("c:/temp/rclone-vs/2020/", "DropboxTineCrypt:/2020/");
    // lsjson();

  }

  private void lsjson() throws InterruptedException, IOException {
    Process process = Runtime.getRuntime().exec("rclone lsjson c:/temp/rclone-vs/2020/2020-04 DropboxTineCrypt:/2020/2020-04");

    InputStreamReader is = new InputStreamReader(process.getInputStream());
    BufferedReader reader = new BufferedReader(is);
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
    }
    Type collectionType = new TypeToken<List<DirectoryEntry>>() {
    }.getType();
    Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    List<DirectoryEntry> enums = gson.fromJson(is, collectionType);
    System.out.println(enums);

    // DirectoryEntry[] enums = gson.fromJson(reader, DirectoryEntry[].class);

    process.waitFor();
    if (process.exitValue() != 0) {
      // logErrorOutput(process);
      // return null;
    }
  }

  private static final Pattern SIZES_DIFFER = Pattern.compile(".*ERROR : (.*): Sizes differ");
  private static final Pattern NOT_IN_LOCAL = Pattern.compile(".*ERROR : (.*): File not in Local file system at \\/\\/\\?\\/(.*)");
  private static final Pattern NOT_IN_REMOTE = Pattern.compile(".*ERROR : (.*): File not in .*'(.*)'");
  private static final Pattern NUMBER_OF_DIFFERENCES = Pattern.compile(".* (.*) differences found");

  @Getter
  private List<String> sizeDiffer;

  @Getter
  private List<RemoteOnlyFile> notInLocal;

  @Getter
  private List<LocalOnlyFile> notInRemote;

  public void check(String localPath, String remotePath) throws IOException, InterruptedException {
    Process process = Runtime.getRuntime().exec("rclone check " + localPath + " " + remotePath);

    InputStreamReader is = new InputStreamReader(process.getErrorStream());
    BufferedReader reader = new BufferedReader(is);

    sizeDiffer = new ArrayList<>();
    notInLocal = new ArrayList<>();
    notInRemote = new ArrayList<>();

    String line;
    while ((line = reader.readLine()) != null) {
      Matcher matcher;
      System.out.println(line);

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

    process.waitFor();
    System.out.println("exit code: " + process.exitValue());
  }

}
