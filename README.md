# rclone: visual compare
This tool provides a JavaFX UI to visually compare two folders (local or remote).
This is done by using the `clone check` command with afterwards parsing of its output.
The UI provides simple functions like deleting one file on one of both sides or showing a preview of images.
The current focus of this project is to compare directories which contain images.

## Features
- Compare two directories (local or remote)
- Show a preview image of a selected file (local/remote)
- Show large image by clicking on the preview image
- Delete/copy file to synchronize directories

## Prerequisites
- rclone binary on PATH
- Java/JavaFX 11

## TODO
- FEATURES
  - Export of the result
  - show preview for different file types than jpg
  - Set path to rclone binary
  - Copy/delete operations for "different content" section
  - Improve local/remote directory chooser
  - Show metadata for files (size etc.)
  - store last paths

- TECHNICAL
  - Sorter/formatter for lists (SyncFile-Object)
  - Use lsjon instead of check (re-implement matching)?
  - Tree instead of ListView
  - Have multi-selection for file operations
  - Don't start second download of a remote image if a download of this image is in progress already