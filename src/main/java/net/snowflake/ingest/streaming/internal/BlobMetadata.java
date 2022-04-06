/*
 * Copyright (c) 2021 Snowflake Computing Inc. All rights reserved.
 */

package net.snowflake.ingest.streaming.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Metadata for a blob that sends to Snowflake as part of the register blob request */
class BlobMetadata {
  private final String path;
  private final String md5;
  private final List<ChunkMetadata> chunks;

  BlobMetadata(String path, String md5, List<ChunkMetadata> chunks) {
    this.path = path;
    this.md5 = md5;
    this.chunks = chunks;
  }

  @JsonProperty("path")
  String getPath() {
    return this.path;
  }

  @JsonProperty("md5")
  String getMD5() {
    return this.md5;
  }

  @JsonProperty("chunks")
  List<ChunkMetadata> getChunks() {
    return this.chunks;
  }
}
