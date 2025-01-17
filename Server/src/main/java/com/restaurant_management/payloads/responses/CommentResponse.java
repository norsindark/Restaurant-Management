package com.restaurant_management.payloads.responses;

import com.restaurant_management.entites.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    private String commentId;
    private String content;
    private String author;
    private String avatar;
    private String blogId;
    private String blogTitle;
    private Timestamp createdAt;
    private List<CommentReplyResponse> replies;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.author = comment.getAuthor().getUsername();
        this.avatar = comment.getAuthor().getAvatar();
        this.blogId = comment.getBlog().getId();
        this.blogTitle = comment.getBlog().getTitle();
        this.createdAt = comment.getCreatedAt();

        this.replies = comment.getReplies() != null ?
                comment.getReplies().stream()
                        .map(CommentReplyResponse::new)
                        .collect(Collectors.toList()) : new ArrayList<>();
    }
}
