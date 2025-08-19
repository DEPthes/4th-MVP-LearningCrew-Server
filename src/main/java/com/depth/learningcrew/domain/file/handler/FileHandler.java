package com.depth.learningcrew.domain.file.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.entity.AttachedFile;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileHandler {

    @Value("${file.save-path}")
    private String savePath;

    @SneakyThrows
    public void saveFile(MultipartFile multipartFile, AttachedFile attachedFile) {
        createDirIfNotExist(savePath);

        File targetFile = Paths.get(savePath, attachedFile.getUuid()).toFile();

        if (targetFile.exists()) {
            throw new RestException(ErrorCode.FILE_ALREADY_EXISTS);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            fileOutputStream.write(multipartFile.getBytes());
            fileOutputStream.flush();
        } catch (Exception e) {
            throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void createDirIfNotExist(String path) {
        File targetDir = Paths.get(path).toFile();
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
    }

    @SneakyThrows
    public Resource loadFileAsResource(AttachedFile attachedFile) {
        File targetFile = Paths.get(savePath, attachedFile.getUuid()).toFile();

        if (!targetFile.exists()) {
            throw new RestException(ErrorCode.FILE_NOT_FOUND);
        }

        return new FileSystemResource(targetFile);
    }

    @SneakyThrows
    public void deleteFile(AttachedFile attachedFile) {
        File targetFile = Paths.get(savePath, attachedFile.getUuid()).toFile();

        if (!targetFile.exists()) {
            log.warn("File {} does not exist", targetFile.getAbsolutePath());
            return;
        }

        if (!targetFile.delete()) {
            throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
