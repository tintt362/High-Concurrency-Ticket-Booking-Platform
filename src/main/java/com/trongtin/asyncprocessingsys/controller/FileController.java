package com.trongtin.asyncprocessingsys.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files/pdf")
@Slf4j
public class FileController {

    @Value("${app.storage.pdf-dir}")
    private String pdfDir;

    // GET /files/pdf/report-{jobId}.pdf
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(pdfDir)
                    .resolve(fileName)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("[FileController] Not found | file={}", fileName);
                return ResponseEntity.notFound().build();
            }

            log.info("[FileController] Download | file={}", fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    // inline = mở trong browser, attachment = tải xuống
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("[FileController] Bad path | file={}", fileName);
            return ResponseEntity.badRequest().build();
        }
    }
}
