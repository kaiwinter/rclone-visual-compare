package com.github.kaiwinter.rclonediff.util;

import com.github.kaiwinter.rclonediff.model.DiffModel;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint;
import com.github.kaiwinter.rclonediff.model.SyncEndpoint.Type;

public class TestFactories {

  public static class SyncEndpointFactory {
    public static SyncEndpoint createLocalEndpoint(String path) {
      return new SyncEndpoint(Type.LOCAL, path);
    }

    public static SyncEndpoint createRemoteEndpoint(String path) {
      return new SyncEndpoint(Type.REMOTE, path);
    }
  }

  public static class DiffModelFactory {
    private SyncEndpoint sourceEndpoint;
    private SyncEndpoint targetEndpoint;

    public DiffModelFactory withLocalSourceEndpoint(String path) {
      this.sourceEndpoint = SyncEndpointFactory.createLocalEndpoint(path);
      return this;
    }

    public DiffModelFactory withRemoteTargetEndpoint(String path) {
      this.targetEndpoint = SyncEndpointFactory.createRemoteEndpoint(path);
      return this;
    }

    public DiffModel create() {
      DiffModel diffModel = new DiffModel();
      diffModel.getSource().setValue(sourceEndpoint);
      diffModel.getTarget().setValue(targetEndpoint);
      return diffModel;
    }
  }
}
