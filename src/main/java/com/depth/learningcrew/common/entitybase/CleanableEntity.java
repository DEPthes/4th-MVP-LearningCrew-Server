package com.depth.learningcrew.common.entitybase;

import com.depth.learningcrew.domain.file.handler.FileHandler;

public interface CleanableEntity {
  void cleanup(FileHandler fileHandler);
}
