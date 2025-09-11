package com.xingmiao.blog.app.service;

public interface DifySyncService {
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
}


