package com.group06.music_app.song.response;
import com.group06.music_app.comment.Comment;
import com.group06.music_app.comment.responses.CommentResponse;
import com.group06.music_app.song.SongLike;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SongResponse {
    private Long id;
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private String lyrics;
    private String fileName;
    private String fileExtension;
    private Long duration;
    private boolean isPublic;
    private boolean isDeleted;

    private int likeCount;
    private int commentCount;
    private List<SongLike> songLikes;
    private List<Comment> comments;


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