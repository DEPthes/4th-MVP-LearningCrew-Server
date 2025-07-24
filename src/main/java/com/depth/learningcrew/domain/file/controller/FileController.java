package com.depth.learningcrew.domain.file.controller;

import com.depth.learningcrew.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "File", description = "파일 다운로드/조회 API")
public class FileController {

    private final FileService fileService;

    @GetMapping("/images/{file_uuId}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String file_uuId
    ) {
        Resource resource = fileService.getResourceById(file_uuId);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "image/jpeg")
                .body(resource);
    }

    @GetMapping("/downloads/{file_uuId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String file_uuId
    ) throws IOException {
        Resource resource = fileService.getResourceById(file_uuId);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Disposition", "attachment; filename=\"" + file_uuId + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(resource);
    }
}
