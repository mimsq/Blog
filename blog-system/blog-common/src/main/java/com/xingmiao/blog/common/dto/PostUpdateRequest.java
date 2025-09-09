package com.xingmiao.blog.common.dto;

import com.xingmiao.blog.common.domain.enums.ContentType;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private ContentType contentType;
    private PostStatus status;
    private Visibility visibility;
    private String password;
    private Long categoryId;
    private String coverImageUrl;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
}


