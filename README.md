# rclone: visual compare
This tool provides a JavaFX UI to visually compare two folders (local or remote).
This is done by using the `clone check` command with afterwards parsing of its output.
The UI provides simple functions like deleting one file on one of both sides or showing a preview of images.
More to come.

## Prerequisites
- rclone binary on PATH
- Java/JavaFX 11

## TODO
- FEATURES
  - Export of the result
  - Sorter/formatter for lists (SyncFile-Object)
  - show preview for different file types than jpg
  - Set path to rclone binary
  - Copy/delete operations for "different content"

- TECHNICAL
  - use lsjon instead of check (re-implement matching)?
  - Tree instead of ListView