package com.group06.music_app_mobile.api_client.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongResponse {
    private long id;
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private String lyrics;
    private boolean isPublic;
    private long duration;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSingerName() {
        return singerName;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getLyrics() {
        return lyrics;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public long getDuration() {
        return duration;
    }
}
