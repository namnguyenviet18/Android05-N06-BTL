package com.group06.music_app.song;

import com.group06.music_app.song.response.FileStoreResult;
import com.group06.music_app.song.response.SongResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
@RequestMapping("/song")
@Tag(name = "Song")
public class SongController {

    @Autowired
    private SongService songService;

    @GetMapping("/like/{song-id}")
    public ResponseEntity<Boolean> handleClickLikeSong(
            @PathVariable(name = "song-id") Long songId,
            Authentication currentUser
    ) {
        return ResponseEntity.ok(songService.handleClickLikeSong(songId, currentUser));
    }

    @Value("${application.file.uploads.photos-output-path}")
    private String uploadDir;

    // API 1: Upload file, nhận MultipartFile
    @PostMapping(value = "file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được rỗng");
        }
        try {
            FileStoreResult fileUrl = songService.storeFile(file);
            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    @GetMapping("file/load")
    public ResponseEntity<Resource> serveFile(
            @RequestParam("fullUrl") String fullUrl) {
        try {
            String relativePath = fullUrl.replaceFirst("/upload/", "");
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

    @DeleteMapping("file/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            songService.deleteFile(fileUrl);
            return ResponseEntity.ok("Xóa file thành công: " + fileUrl);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Đường dẫn không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xóa file: " + e.getMessage());
        }
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addMusic(
            @RequestParam("songName") String songName,
            @RequestParam("authorName") String authorName,
            @RequestParam("singerName") String singerName,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("lyricFile") MultipartFile lyricFile,
            @RequestParam("isPublic") boolean isPublic,
            Authentication currentUser
            ) {

        // Validate inputs
        if (songName == null || songName.isEmpty()) {
            return ResponseEntity.badRequest().body("Tên bài hát không được rỗng");
        }
        if (authorName == null || authorName.isEmpty()) {
            return ResponseEntity.badRequest().body("Tên tác giả không được rỗng");
        }
        if (singerName == null || singerName.isEmpty()) {
            return ResponseEntity.badRequest().body("Tên ca sĩ không được rỗng");
        }
        if (audioFile.isEmpty() || coverImage.isEmpty() || lyricFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Các file không được rỗng");
        }

        // Additional validation (optional): Check file types
        if (!audioFile.getOriginalFilename().endsWith(".mp3")) {
            return ResponseEntity.badRequest().body("File audio phải là định dạng mp3");
        }
        if (!coverImage.getOriginalFilename().matches(".*\\.(png|jpeg|jpg)")) {
            return ResponseEntity.badRequest().body("File ảnh bìa phải là định dạng png hoặc jpeg");
        }
        if (!lyricFile.getContentType().startsWith("application/json")) {
            return ResponseEntity.badRequest().body("File lyric phải là định dạng json");
        }


        FileStoreResult audioFilePath = null;
        FileStoreResult coverImagePath = null;
        FileStoreResult lyricFilePath = null;


        try {
            // Store files only after validation passes
            audioFilePath = songService.storeFile(audioFile);
            coverImagePath = songService.storeFile(coverImage);
            lyricFilePath = songService.storeFile(lyricFile);

            // Save song to database
            Song savedSong = songService.saveSong(
                    songName, authorName, singerName,
                    audioFilePath.getFilePath(), coverImagePath.getFilePath(), lyricFilePath.getFilePath(),
                    isPublic, audioFilePath.getDuration(), currentUser
            );
            // Return success message
            String response = String.format("Bài hát đã được thêm: Audio: %s, Cover: %s, Lyric: %s",
                    savedSong.getAudioUrl(), savedSong.getCoverImageUrl(), savedSong.getLyrics());

            System.out.println(response);
            SongResponse responseDTO = songService.toDto(savedSong);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            if (audioFilePath != null) {
                songService.deleteFile(audioFilePath.getFilePath());
            }
            if (coverImagePath != null) {
                songService.deleteFile(coverImagePath.getFilePath());
            }
            if (lyricFilePath != null) {
                songService.deleteFile(lyricFilePath.getFilePath());
            }
            return ResponseEntity.status(500).body("Lỗi khi thêm bài hát: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<SongResponse>> getAllSongs(
            Authentication currentUser
    ) {
        try {
            List<SongResponse> songs = songService.getAllSongs(currentUser);
            if (songs.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSong(@PathVariable("id") Long songId) {
        try {
            songService.deleteSong(songId);
            return ResponseEntity.ok("Xóa bài hát thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xóa bài hát: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongDetails(
            @PathVariable("id") Long songId,
            Authentication currentUser
    ) {
        try {
            SongResponse song = songService.getSongById(songId, currentUser);
            return ResponseEntity.ok(song);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/liked")
    public ResponseEntity<List<SongResponse>> getLikedSong(
            Authentication currentUser
    ) {
        return ResponseEntity.ok(songService.getLikedSong(currentUser));
    }
}
