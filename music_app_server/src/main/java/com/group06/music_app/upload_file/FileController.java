package com.group06.music_app.upload_file;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private com.group06.music_app.upload_file.FileStorageService fileStorageService;

    @Value("${application.file.uploads.photos-output-path}")
    private String uploadDir;

    // API 1: Upload file, nhận MultipartFile
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được rỗng");
        }
        try {
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    @GetMapping("/load")
    public ResponseEntity<Resource> serveFile(
            @RequestParam("fullUrl") String fullUrl) {
        try {
            String relativePath = fullUrl.replaceFirst("/api/v1/upload/", "");
            if (!relativePath.contains("/")) {
                return ResponseEntity.badRequest().body(null);
            }

            // Tách subDir và fileName
            String[] parts = relativePath.split("/", 2);
            if (parts.length != 2) {
                return ResponseEntity.badRequest().body(null);
            }
            String subDir = parts[0];
            String fileName = parts[1];

            // Kiểm tra subDir hợp lệ
            if (!subDir.equals("images") && !subDir.equals("audios") && !subDir.equals("json")) {
                return ResponseEntity.badRequest().body(null);
            }

            // Tạo đường dẫn file
            Path filePath = Paths.get(uploadDir + "/" + subDir + "/" + fileName);
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = determineContentType(subDir);
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // Hiển thị trực tiếp thay vì tải về
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    private String determineContentType(String type) {
        return switch (type) {
            case "images" -> "image/jpeg";
            case "audios" -> "audio/mpeg";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }
    // API 2: Tải file chỉ với fileName
//    @GetMapping("/upload/{fileName:.+}")
//    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
//        try {
//            String[] subDirs = {"images", "audios", "json"};
//            Path filePath = null;
//            String foundType = null;
//
//            for (String subDir : subDirs) {
//                Path potentialPath = Paths.get(uploadDir + "/" + subDir + "/" + fileName);
//                if (Files.exists(potentialPath) && Files.isReadable(potentialPath)) {
//                    filePath = potentialPath;
//                    foundType = subDir;
//                    break;
//                }
//            }
//
//            if (filePath == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            Resource resource = new UrlResource(filePath.toUri());
//            if (resource.exists() && resource.isReadable()) {
//                String contentType = Files.probeContentType(filePath);
//                if (contentType == null) {
//                    contentType = determineContentType(foundType);
//                }
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(contentType))
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                        .body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//
//    private String determineContentType(String type) {
//        return switch (type) {
//            case "images" -> "image/jpeg";
//            case "audios" -> "audio/mpeg";
//            case "json" -> "application/json";
//            default -> "application/octet-stream";
//        };
//    }
}