package com.group06.music_app.song.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@Builder
public class FileStoreResult {

    private String filePath;
    private Long duration;

    public FileStoreResult(String filePath, Long duration) {
        this.filePath = filePath;
        this.duration = duration;
    }

}