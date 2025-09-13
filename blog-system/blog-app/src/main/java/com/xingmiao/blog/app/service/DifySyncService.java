package com.xingmiao.blog.app.service;

public interface DifySyncService {
    // Category相关方法
    String createKnowledgeBaseInDifyByCategoryId(Long categoryId);

    boolean updateKnowledgeBaseInDifyByCategoryId(Long categoryId);

    void deleteCategoryAsync(Long categoryId);

    /**
     * 幂等同步分类到 Dify：
     * - isActive=true 且无 datasetId => 创建
     * - isActive=true 且有 datasetId => 更新
     * - isActive=false => 删除（有 id 则删，无 id 则忽略/直接物理删）
     */
    void syncCategory(Long categoryId);
    
    // Post相关方法
    /**
     * 创建文章到Dify知识库
     */
    void createPostToDify(Long postId);
    
    /**
     * 更新Dify中的文章文档
     */
    void updatePostInDify(Long postId);
    
    /**
     * 从Dify删除文章文档
     */
    void deletePostFromDify(Long postId);
    
    /**
     * 幂等同步文章到Dify：
     * - 无difyDocumentId或同步失败 => 创建
     * - 已同步 => 更新
     */
    void syncPost(Long postId);
}


