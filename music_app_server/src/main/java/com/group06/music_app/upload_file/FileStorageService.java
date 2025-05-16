package com.group06.music_app.upload_file;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${application.file.uploads.photos-output-path}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir + "/images"));
            Files.createDirectories(Paths.get(uploadDir + "/audios"));
            Files.createDirectories(Paths.get(uploadDir + "/json"));
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String mimeType = file.getContentType();
            System.out.println(mimeType);
            String subDir = getSubDirectory(mimeType);
            System.out.println(subDir);
            String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            Path filePath = Paths.get(uploadDir, subDir, fileName);
            Files.write(filePath, file.getBytes());
            return "/api/v1/upload/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file!", e);
        }
    }

    private String getSubDirectory(String mimeType) {
        if (mimeType == null) {
            throw new IllegalArgumentException("Mimetype không hợp lệ!");
        }
        if (mimeType.startsWith("image/")) {
            return "images";
        } else if (mimeType.startsWith("audio/")) {
            return "audios";
        } else if (mimeType.equals("application/json")) {
            return "json";
        } else {
            throw new IllegalArgumentException("Loại file không được hỗ trợ: " + mimeType);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}