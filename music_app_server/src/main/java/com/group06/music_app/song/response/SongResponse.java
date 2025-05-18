package com.group06.music_app.song.response;
import lombok.Data;

@Data
public class SongResponse {
    private Long id;
    private String createdDate;
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private String lyrics;
    private String fileName;
    private String fileExtension;
    private boolean isPublic;
    private boolean isDeleted;

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}