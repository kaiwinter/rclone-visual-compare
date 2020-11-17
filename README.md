# rclone: visual compare
This tool provides a JavaFX UI to visually compare two folders (local or remote).
This is done by using the `clone check` command with afterwards parsing of its output.
The UI provides simple functions like deleting one file on one of both sides or showing a preview of images.
The current focus of this project is to compare directories which contain images.

[![Build Status](https://travis-ci.org/kaiwinter/rclone-visual-compare.svg?branch=master)](https://travis-ci.org/kaiwinter/rclone-visual-compare)

## Features
- Compare two directories (local or remote)
- Show a preview image of a selected file (local/remote)
- Show large image by clicking on the preview image
- Delete/copy file to synchronize directories

## Prerequisites
- Java/JavaFX 11

## Screenshot
![Screenshot](../assets/screenshot.png?raw=true)

## TODO
- FEATURES
  - Show metadata for files (size etc.)
  - Bind counter in list titles to real number of items (they change on delete/copy operations)

- TECHNICAL
  - Sorter/formatter for lists (SyncFile-Object)
  - Use lsjon instead of check (re-implement matching)?
  - Tree instead of ListView?
  - Don't start second download of a remote image if a download of this image is in progress already
  - "Copy to xyz": if source is remote and file was copied to local for preview already, copy the temp file instead of starting a new download
  - Cancel running copy operations on application exit
