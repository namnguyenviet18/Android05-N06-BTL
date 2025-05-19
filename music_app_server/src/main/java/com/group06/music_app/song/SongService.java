package com.group06.music_app.song;

import com.group06.music_app.comment.Comment;
import com.group06.music_app.comment.CommentLike;
import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import com.group06.music_app.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    @Autowired
    private final SongRepository songRepository;

    @Autowired
    private final UserRepository userRepository;

    public Song findSongById(Long id) {
        Optional<Song> songOpt = songRepository.findById(id);
        if(songOpt.isEmpty()) {
            throw new EntityNotFoundException("Song not found!");
        }
        return songOpt.get();
    }

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
            return "/upload/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file!", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/upload/")) {
                throw new IllegalArgumentException("Đường dẫn file không hợp lệ: " + fileUrl);
            }

            String[] parts = fileUrl.split("/");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Đường dẫn file không đúng định dạng: " + fileUrl);
            }
            String subDir = parts[2]; // e.g., "images", "audios", "json"
            String fileName = parts[3]; // e.g., "uuid_file.mp3"

            // Construct the full file path
            Path filePath = Paths.get(uploadDir, subDir, fileName);

            // Check if file exists and delete it
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Đã xóa file: " + filePath);
            } else {
                System.out.println("File không tồn tại: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file: " + fileUrl, e);
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

    @Transactional
    public Song saveSong(String songName, String authorName, String singerName,
                         String audioFilePath, String coverImagePath, String lyricFilePath,
                         boolean isPublic, Authentication currentUser) throws Exception {
        User user = (User) currentUser.getPrincipal();

        // Extract file information to get filename, fileextension
        Path audioPath = Paths.get(audioFilePath);
        String audioFileName = audioPath.getFileName().toString();
        String audioFileExtension = audioFileName.substring(audioFileName.lastIndexOf("."));

        // Build and save the song entity
        Song song = Song.builder()
                .name(songName)
                .authorName(authorName)
                .singerName(singerName)
                .audioUrl(audioFilePath)
                .coverImageUrl(coverImagePath)
                .lyrics(lyricFilePath)
                .isPublic(isPublic)
                .isDeleted(false)
                .fileName(audioFileName)
                .fileExtension(audioFileExtension)
                .user(user)
                .build();

        return songRepository.save(song);
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getAllSongs() {
        return songRepository.findByIsDeletedFalse().stream().map(song -> {
            SongResponse dto = new SongResponse();
            dto.setId(song.getId());
            dto.setCreatedDate(song.getCreatedDate().toString());
            dto.setName(song.getName());
            dto.setAuthorName(song.getAuthorName());
            dto.setSingerName(song.getSingerName());
            dto.setAudioUrl(song.getAudioUrl());
            dto.setCoverImageUrl(song.getCoverImageUrl());
            dto.setLyrics(song.getLyrics());
            dto.setFileName(song.getFileName());
            dto.setFileExtension(song.getFileExtension());
            dto.setIsPublic(song.isPublic());
            dto.setIsDeleted(song.isDeleted());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SongResponse getSongById(Long songId) throws Exception {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Bài hát không tồn tại"));
        if (song.isDeleted()) {
            throw new Exception("Bài hát đã bị xóa");
        }
        SongResponse dto = new SongResponse();
        dto.setId(song.getId());
        dto.setCreatedDate(song.getCreatedDate().toString());
        dto.setName(song.getName());
        dto.setAuthorName(song.getAuthorName());
        dto.setSingerName(song.getSingerName());
        dto.setAudioUrl(song.getAudioUrl());
        dto.setCoverImageUrl(song.getCoverImageUrl());
        dto.setLyrics(song.getLyrics());
        dto.setFileName(song.getFileName());
        dto.setFileExtension(song.getFileExtension());
        dto.setIsPublic(song.isPublic());
        dto.setIsDeleted(song.isDeleted());
        return dto;
    }

    @Transactional
    public void deleteSong(Long songId) throws Exception {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Bài hát không tồn tại"));
        if (song.isDeleted()) {
            throw new Exception("Bài hát đã bị xóa");
        }

        // Xóa các tệp liên quan
        deleteFile(song.getAudioUrl());
        deleteFile(song.getCoverImageUrl());
        deleteFile(song.getLyrics());

        // Đánh dấu bài hát là đã xóa
        song.setDeleted(true);
        songRepository.save(song);
    }

    public Boolean handleClickLikeSong(Long songId, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        Song song = findSongById(songId);
        Optional<SongLike> existingLikeOpt = song.getSongLikes()
                .stream()
                .filter(like -> like.getUser().getId().equals(user.getId()))
                .findFirst();
        if(existingLikeOpt.isPresent()) {
            SongLike existingLike = existingLikeOpt.get();
            song.getSongLikes().remove(existingLike);
        } else {
            SongLike songLike = SongLike.builder()
                    .user(user)
                    .song(song)
                    .build();
            song.getSongLikes().add(songLike);
        }
        songRepository.save(song);
        return existingLikeOpt.isEmpty();
    }

}
