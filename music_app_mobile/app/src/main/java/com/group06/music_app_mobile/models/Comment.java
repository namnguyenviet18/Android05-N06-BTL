package com.group06.music_app_mobile.models;

import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Comment extends BaseEntity {
    private User user;
    private String content;
    private List<Comment> descendants;
    private Comment parent;
    private boolean liked;
    private long likeCount;
    private long descendantCount;
    private List<CommentLike> likes;
    private boolean isDeleted;

    private boolean showDescendants = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment that = (Comment) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
