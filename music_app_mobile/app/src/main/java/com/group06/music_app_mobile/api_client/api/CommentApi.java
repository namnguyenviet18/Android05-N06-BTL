package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.api_client.requests.CommentRequest;
import com.group06.music_app_mobile.models.Comment;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentApi {

    @POST("comment")
    Call<Comment> createComment(@Body CommentRequest request);

    @GET("comment/descendants/{root-id}")
    Call<List<Comment>> getDescendants(@Path("root-id") Long commentRootId);

    @GET("comment/song/{song-id}")
    Call<List<Comment>> getCommentsBySong(@Path("song-id") Long songId);

    @GET("comment/like/{comment-id}")
    Call<Void> clickLikeComment(@Path("comment-id") Long commentId);

}
