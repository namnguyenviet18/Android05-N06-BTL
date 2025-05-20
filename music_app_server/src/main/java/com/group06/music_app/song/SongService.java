package com.group06.music_app.song;
import com.group06.music_app.song.response.FileStoreResult;
import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    @Autowired
    private final SongRepository songRepository;

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

    public FileStoreResult storeFile(MultipartFile file) {
        try {
            String mimeType = file.getContentType();
            String subDir = getSubDirectory(mimeType);
            String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
            Path filePath = Paths.get(uploadDir, subDir, fileName);
            Files.write(filePath, file.getBytes());

            Long duration = null;
            if (mimeType != null && mimeType.startsWith("audio/")) {
                duration = getAudioDuration(file);
            }

            return new FileStoreResult("/upload/" + subDir + "/" + fileName, duration);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file!", e);
        }
    }

    private Long getAudioDuration(MultipartFile file) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            parser.parse(file.getInputStream(), handler, metadata, new org.apache.tika.parser.ParseContext());

            String durationStr = metadata.get("xmpDM:duration");
            if (durationStr != null) {
                return (long) (Double.parseDouble(durationStr) * 1000);
            }
            return null;
        } catch (IOException | SAXException | TikaException e) {
            System.err.println("Lỗi khi lấy thời lượng audio: " + e.getMessage());
            return null;
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
                         boolean isPublic, long duration, Authentication currentUser) throws Exception {
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
                .duration(duration)
                .user(user)
                .build();

        return songRepository.save(song);
    }

    @Transactional(readOnly = true)
    public List<SongResponse> getAllSongs() {
        return songRepository.findByIsDeletedFalse().stream().map(song -> SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .authorName(song.getAuthorName())
                .singerName(song.getSingerName())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .lyrics(song.getLyrics())
                .isPublic(song.isPublic())
                .fileName(song.getFileName())
                .fileExtension(song.getFileExtension())
                .duration(song.getDuration())
                .likeCount(song.getSongLikes() != null ? song.getSongLikes().size() : 0)
                .commentCount(song.getComments() != null ? song.getComments().size() : 0)
                .comments(Collections.emptyList())
                .songLikes(Collections.emptyList())
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SongResponse getSongById(Long songId) throws Exception {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new Exception("Bài hát không tồn tại"));
        if (song.isDeleted()) {
            throw new Exception("Bài hát đã bị xóa");
        }
        return SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .authorName(song.getAuthorName())
                .singerName(song.getSingerName())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .lyrics(song.getLyrics())
                .isPublic(song.isPublic())
                .fileName(song.getFileName())
                .fileExtension(song.getFileExtension())
                .duration(song.getDuration())
                .likeCount(song.getSongLikes() != null ? song.getSongLikes().size() : 0)
                .commentCount(song.getComments() != null ? song.getComments().size() : 0)
                .comments(Collections.emptyList())
                .songLikes(Collections.emptyList())
                .build();
    }

    public SongResponse toDto(Song song) {
        return SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .authorName(song.getAuthorName())
                .singerName(song.getSingerName())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .lyrics(song.getLyrics())
                .isPublic(song.isPublic())
                .fileName(song.getFileName())
                .fileExtension(song.getFileExtension())
                .duration(song.getDuration())
                .build();
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

}
