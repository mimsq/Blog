package com.xingmiao.blog.dto;

import com.xingmiao.blog.domain.enums.ContentType;
import com.xingmiao.blog.domain.enums.PostStatus;
import com.xingmiao.blog.domain.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "别名不能为空")
    @Size(max = 200, message = "别名长度不能超过200个字符")
    private String slug;

    private String excerpt;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "内容类型不能为空")
    private ContentType contentType;

    @NotNull(message = "状态不能为空")
    private PostStatus status;

    @NotNull(message = "可见性不能为空")
    private Visibility visibility;

    private String password;

    private Long categoryId;

    private String coverImageUrl;

    private String metaTitle;

    private String metaDescription;

    private String metaKeywords;
}


