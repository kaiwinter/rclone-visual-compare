package com.github.kaiwinter.rclonediff.core;

import com.github.kaiwinter.rclonediff.model.SyncEndpoint;

import lombok.Data;

/**
 * Model for the {@link DiffController}.
 */
@Data
public class DiffModel {
  private SyncEndpoint source = new SyncEndpoint(SyncEndpoint.Type.LOCAL, "z:/2017/");
  private SyncEndpoint target = new SyncEndpoint(SyncEndpoint.Type.REMOTE, "DropboxTineCrypt:/2017/");
}
