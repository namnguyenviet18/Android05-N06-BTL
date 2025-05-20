package com.group06.music_app.song.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileStoreResult {

    private String filePath;
    private Long duration;

    public FileStoreResult(String filePath, Long duration) {
        this.filePath = filePath;
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public Long getDuration() {
        return duration;
    }
}
