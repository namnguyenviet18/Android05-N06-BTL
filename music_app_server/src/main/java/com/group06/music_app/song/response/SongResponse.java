package com.group06.music_app.song.response;
import com.group06.music_app.comment.Comment;
import com.group06.music_app.comment.responses.CommentResponse;
import com.group06.music_app.song.SongLike;
import com.group06.music_app.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private long likeCount;
    private long commentCount;
    private long duration;
    private List<SongLike> likes;
    private List<CommentResponse> comments;
    private boolean isPublic;
    private boolean isDeleted;
    private boolean liked;
    private UserResponse user;
    private long viewCount;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdBy;
    private Long lastModifiedBy;


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