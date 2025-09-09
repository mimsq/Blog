package com.xingmiao.blog.common.dto;

import com.xingmiao.blog.common.domain.enums.ContentType;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章创建请求")
public class PostCreateRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "文章标题", example = "我的第一篇博客文章", required = true)
    private String title;

    @Size(max = 200, message = "别名长度不能超过200个字符")
    @Schema(description = "文章别名，用于URL友好显示，如果不提供将自动生成", example = "my-first-blog-post")
    private String slug;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    @Schema(description = "文章摘要", example = "这是文章的简要描述...")
    private String excerpt;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "文章内容", example = "# 文章标题\n\n这是文章的主要内容...", required = true)
    private String content;

    @Builder.Default
    @Schema(description = "内容类型", example = "MARKDOWN", allowableValues = {"MARKDOWN", "HTML", "RICH_TEXT"})
    private ContentType contentType = ContentType.MARKDOWN;

    @Builder.Default
    @Schema(description = "文章状态", example = "DRAFT", allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"})
    private PostStatus status = PostStatus.DRAFT;

    @Builder.Default
    @Schema(description = "文章可见性", example = "PUBLIC", allowableValues = {"PUBLIC", "PRIVATE", "PASSWORD"})
    private Visibility visibility = Visibility.PUBLIC;

    @Size(max = 50, message = "密码长度不能超过50个字符")
    @Schema(description = "文章密码，当可见性为PASSWORD时必填", example = "mypassword123")
    private String password;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverImageUrl;

    @Size(max = 200, message = "SEO标题长度不能超过200个字符")
    @Schema(description = "SEO标题", example = "我的第一篇博客文章 - 星喵博客")
    private String metaTitle;

    @Size(max = 500, message = "SEO描述长度不能超过500个字符")
    @Schema(description = "SEO描述", example = "这是我的第一篇博客文章，分享我的学习心得...")
    private String metaDescription;

    @Size(max = 200, message = "SEO关键词长度不能超过200个字符")
    @Schema(description = "SEO关键词，多个关键词用逗号分隔", example = "博客,学习,技术")
    private String metaKeywords;
}


