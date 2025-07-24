package com.depth.learningcrew.domain.file.service;

import com.depth.learningcrew.domain.file.entity.AttachedFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.file.repository.AttachedFileRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final AttachedFileRepository attachedFileRepository;
    private final FileHandler fileHandler;

    @Transactional(readOnly = true)
    public Resource getResourceById(String fileId) {
        AttachedFile found = attachedFileRepository.findById(fileId)
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

        return fileHandler.loadFileAsResource(found);
    }
}
