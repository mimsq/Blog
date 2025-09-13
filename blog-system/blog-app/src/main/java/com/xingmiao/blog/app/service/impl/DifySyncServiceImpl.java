package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.repository.PostRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.common.domain.entity.Category;
import com.xingmiao.blog.common.domain.entity.Post;
import com.xingmiao.blog.common.domain.enums.PostStatus;
import com.xingmiao.blog.common.domain.enums.SyncStatus;
import com.xingmiao.blog.common.domain.enums.Visibility;
import com.xingmiao.blog.ai.client.DifyApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DifySyncServiceImpl implements DifySyncService {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final DifyApiClient difyApiClient;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public boolean updateKnowledgeBaseInDifyByCategoryId(Long categoryId) {
            Optional<Category> optional = categoryRepository.findById(categoryId);
            if (optional.isEmpty()) {
                log.warn("分类未找到,分类ID: {}", categoryId);
                return false;
            }
            Category category = optional.get();
            String datasetId = category.getDifyDatasetId();

            if (datasetId == null || datasetId.isEmpty()) {
                try {
                    log.info("分类无Dify知识库ID，转为创建，分类ID:{} 名称:{}", categoryId, category.getName());
                    String response = difyApiClient.createDataset(category.getName(), category.getDescription());
                    String createdId = extractDatasetId(response);
                    if (createdId == null || createdId.isEmpty()) {
                        throw new RuntimeException("创建知识库成功但未返回ID");
                    }
                    applyUpsertSuccess(category, createdId);
                    return true;
                } catch (Exception e) {
                    log.error("创建知识库失败，分类ID:{} 名称:{}", categoryId, category.getName(), e);
                    applyUpsertFailure(categoryId, e.getMessage());
                    return false;
                }
            }
            try {
                    log.info("开始根据分类更新知识库,分类ID:{} 名称:{}", categoryId, category.getName());
                    difyApiClient.updateDataset(datasetId, category.getName(), category.getDescription());
                    applyUpsertSuccess(category, datasetId);
                    return true;
            }catch (Exception e){
                    log.error("更新知识库失败，分类ID:{} 名称:{}", categoryId, category.getName(), e);
                    applyUpsertFailure(categoryId, e.getMessage());
                    return false;
            }
    }

    @Override
    @Async
    public void deleteCategoryAsync(Long categoryId) {
        try {
            Optional<Category> optional = categoryRepository.findById(categoryId);
            if (optional.isEmpty()) {
                // 若记录已物理删除，这里视为幂等成功
                log.info("Category already deleted: {}", categoryId);
                return;
            }
            Category category = optional.get();

            // 如果没有 difyDatasetId，则认为外部已不存在，直接物理删除
            if (category.getDifyDatasetId() == null || category.getDifyDatasetId().isEmpty()) {
                categoryRepository.deleteById(categoryId);
                return;
            }

            // 调用 Dify 删除
            difyApiClient.deleteDataset(category.getDifyDatasetId());

            // 成功后物理删除本地
            categoryRepository.deleteById(categoryId);
        } catch (Exception ex) {
            log.error("Dify delete failed for category {}", categoryId, ex);
            // 写回失败信息供重试
            applyDeleteFailure(categoryId, ex.getMessage());
        }
    }

    @Override
    @Async
    public String createKnowledgeBaseInDifyByCategoryId(Long categoryId) {
        Optional<Category> optional = categoryRepository.findById(categoryId);
        if (optional.isEmpty()) {
            log.warn("分类未找到,分类ID: {}", categoryId);
            return null;
        }
        Category category = optional.get();
       try {
           log.info("开始根据分类创建知识库,分类ID:{} 名称:{}", categoryId, category.getName());
           String KnowledgeBaseName = category.getName();
           String response = difyApiClient.createDataset(KnowledgeBaseName, category.getDescription());

           String difyBaseId = extractDatasetId(response);
           if (difyBaseId == null || difyBaseId.isEmpty()) {
               throw new RuntimeException("创建知识库成功但未返回ID");
           }
           applyUpsertSuccess(category, difyBaseId);
           return difyBaseId;
       }catch (IOException e){
       log.error("创建知识库失败，分类ID:{} 名称:{}", categoryId, category.getName(), e);
           applyUpsertFailure(categoryId, e.getMessage());
           throw new RuntimeException("创建知识库失败: " + e.getMessage(), e);
       }
    }

    @Override
    @Async
    public void syncCategory(Long categoryId) {
        Optional<Category> optional = categoryRepository.findById(categoryId);
        if (optional.isEmpty()) {
            log.warn("分类未找到,分类ID: {}", categoryId);
            return;
        }
        Category category = optional.get();
        String datasetId = category.getDifyDatasetId();
        Boolean isActive = category.getIsActive();

        if (Boolean.TRUE.equals(isActive)) {
            if (datasetId == null || datasetId.isEmpty()) {
                // 创建
                try {
                    log.info("[Sync] 创建知识库，分类ID:{} 名称:{}", categoryId, category.getName());
                    String response = difyApiClient.createDataset(category.getName(), category.getDescription());
                    String createdId = extractDatasetId(response);
                    if (createdId == null || createdId.isEmpty()) {
                        throw new RuntimeException("创建知识库成功但未返回ID");
                    }
                    applyUpsertSuccess(category, createdId);
                } catch (Exception e) {
                    log.error("[Sync] 创建知识库失败，分类ID:{} 名称:{}", categoryId, category.getName(), e);
                    applyUpsertFailure(categoryId, e.getMessage());
                }
            } else {
                // 更新
                try {
                    log.info("[Sync] 更新知识库，分类ID:{} 名称:{} datasetId:{}", categoryId, category.getName(), datasetId);
                    difyApiClient.updateDataset(datasetId, category.getName(), category.getDescription());
                    applyUpsertSuccess(category, datasetId);
                } catch (Exception e) {
                    log.error("[Sync] 更新知识库失败，分类ID:{} 名称:{} datasetId:{}", categoryId, category.getName(), datasetId, e);
                    applyUpsertFailure(categoryId, e.getMessage());
                }
            }
        } else {
            // 删除
            try {
                if (datasetId != null && !datasetId.isEmpty()) {
                    log.info("[Sync] 删除知识库，分类ID:{} 名称:{} datasetId:{}", categoryId, category.getName(), datasetId);
                    difyApiClient.deleteDataset(datasetId);
                } else {
                    log.info("[Sync] 删除知识库跳过外部，分类ID:{} 名称:{}，无datasetId", categoryId, category.getName());
                }
                categoryRepository.deleteById(categoryId);
            } catch (Exception e) {
                log.error("[Sync] 删除知识库失败，分类ID:{} 名称:{} datasetId:{}", categoryId, category.getName(), datasetId, e);
                applyDeleteFailure(categoryId, e.getMessage());
            }
        }
    }

    @Transactional
    protected void applyUpsertSuccess(Category category, String datasetId) {
        category.setDifyDatasetId(datasetId);
        category.setSyncStatus(SyncStatus.SYNCED);
        category.setSyncError(null);
        categoryRepository.save(category);
    }

    @Transactional
    protected void applyUpsertFailure(Long categoryId, String errorMsg) {
        categoryRepository.findById(categoryId).ifPresent(c -> {
            c.setSyncStatus(SyncStatus.UNSYNCED);
            c.setSyncError(truncate(errorMsg));
            categoryRepository.save(c);
        });
    }

    @Transactional
    protected void applyDeleteFailure(Long categoryId, String errorMsg) {
        categoryRepository.findById(categoryId).ifPresent(c -> {
            c.setSyncError(truncate(errorMsg));
            // 删除失败保持记录以便用户重试
            categoryRepository.save(c);
        });
    }

    private String truncate(String msg) {
        if (msg == null) return null;
        return msg.length() > 950 ? msg.substring(0, 950) : msg;
    }

    private String extractDatasetId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String id = root.path("id").asText(null);
            if (id == null || id.isEmpty()) {
                // 兼容某些返回结构
                id = root.path("data").path("id").asText(null);
            }
            return id;
        } catch (Exception e) {
            log.warn("解析 Dify 响应失败，无法获取 datasetId: {}", e.getMessage());
            return null;
        }
    }

    // ========== Post相关方法实现 ==========

    @Override
    @Async
    public void createPostToDify(Long postId) {
        // 在事务中重新加载Post，确保关联对象被正确加载
        Post post = loadPostWithCategory(postId);
        if (post == null) {
            log.warn("文章未找到,文章ID: {}", postId);
            return;
        }
        
        // 检查是否满足同步条件
        if (!shouldSyncPost(post)) {
            log.info("文章不满足同步条件，跳过同步，文章ID:{} 标题:{}", postId, post.getTitle());
            return;
        }
        
        // 检查是否已经同步
        if (post.getDifyDocumentId() != null && !post.getDifyDocumentId().isEmpty()) {
            log.info("文章已同步，跳过创建，文章ID:{} 标题:{}", postId, post.getTitle());
            return;
        }
        
        try {
            // 获取分类对应的知识库ID
            String datasetId = getDatasetIdForPost(post);
            if (datasetId == null) {
                log.warn("文章分类无对应知识库，跳过同步，文章ID:{} 标题:{}", postId, post.getTitle());
                return;
            }
            
            log.info("开始创建Dify文档，文章ID:{} 标题:{}", postId, post.getTitle());
            
            // 构建完整的dataConfig，参考API文档和文件创建的成功经验
            Map<String, Object> dataConfig = new HashMap<>();
            dataConfig.put("indexing_technique", "high_quality");
            
            // 构建process_rule，参考文件创建方法的成功配置
            Map<String, Object> processRule = new HashMap<>();
            processRule.put("mode", "automatic");
            
            // 构建rules（即使mode是automatic，也需要提供）
            Map<String, Object> rules = new HashMap<>();
            
            // 预处理规则
            List<Map<String, Object>> preProcessingRules = new ArrayList<>();
            Map<String, Object> rule1 = new HashMap<>();
            rule1.put("id", "remove_extra_spaces");
            rule1.put("enabled", true);
            preProcessingRules.add(rule1);
            
            Map<String, Object> rule2 = new HashMap<>();
            rule2.put("id", "remove_urls_emails");
            rule2.put("enabled", true);
            preProcessingRules.add(rule2);
            
            rules.put("pre_processing_rules", preProcessingRules);
            
            // 分段规则
            Map<String, Object> segmentation = new HashMap<>();
            segmentation.put("separator", "\n");
            segmentation.put("max_tokens", 1000);
            segmentation.put("parent_mode", "full-doc");
            rules.put("segmentation", segmentation);
            
            processRule.put("rules", rules);
            dataConfig.put("process_rule", processRule);
            
            String difyDocumentId = difyApiClient.createDocumentByText(
                datasetId, 
                post.getTitle(), 
                buildPostContent(post),
                dataConfig
            );
            
            applyPostUpsertSuccess(post, difyDocumentId);
            log.info("Dify文档创建成功，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), difyDocumentId);
            
        } catch (Exception e) {
            log.error("创建Dify文档失败，文章ID:{} 标题:{}", postId, post.getTitle(), e);
            applyPostUpsertFailure(postId, e.getMessage());
        }
    }

    @Override
    @Async
    public void updatePostInDify(Long postId) {
        Post post = loadPostWithCategory(postId);
        if (post == null) {
            log.warn("文章未找到,文章ID: {}", postId);
            return;
        }
        
        // 检查是否满足同步条件
        if (!shouldSyncPost(post)) {
            log.info("文章不满足同步条件，跳过更新，文章ID:{} 标题:{}", postId, post.getTitle());
            return;
        }
        
        // 检查是否已经同步
        if (post.getDifyDocumentId() == null || post.getDifyDocumentId().isEmpty()) {
            log.info("文章未同步，转为创建，文章ID:{} 标题:{}", postId, post.getTitle());
            createPostToDify(postId);
            return;
        }
        
        try {
            // 获取分类对应的知识库ID
            String datasetId = getDatasetIdForPost(post);
            if (datasetId == null) {
                log.warn("文章分类无对应知识库，跳过更新，文章ID:{} 标题:{}", postId, post.getTitle());
                return;
            }
            
            log.info("开始更新Dify文档，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId());
            difyApiClient.updateDocumentByText(
                datasetId, 
                post.getDifyDocumentId(), 
                post.getTitle(), 
                buildPostContent(post)
            );
            
            applyPostUpsertSuccess(post, post.getDifyDocumentId());
            log.info("Dify文档更新成功，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId());
            
        } catch (Exception e) {
            log.error("更新Dify文档失败，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId(), e);
            applyPostUpsertFailure(postId, e.getMessage());
        }
    }

    @Override
    @Async
    public void deletePostFromDify(Long postId) {
        Post post = loadPostWithCategory(postId);
        if (post == null) {
            log.info("文章已删除，跳过Dify删除，文章ID: {}", postId);
            return;
        }
        
        // 检查是否有Dify文档ID
        if (post.getDifyDocumentId() == null || post.getDifyDocumentId().isEmpty()) {
            log.info("文章无Dify文档ID，跳过删除，文章ID:{} 标题:{}", postId, post.getTitle());
            return;
        }
        
        try {
            // 获取分类对应的知识库ID
            String datasetId = getDatasetIdForPost(post);
            if (datasetId == null) {
                log.warn("文章分类无对应知识库，跳过删除，文章ID:{} 标题:{}", postId, post.getTitle());
                return;
            }
            
            log.info("开始删除Dify文档，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId());
            difyApiClient.deleteDocument(datasetId, post.getDifyDocumentId());
            
            applyPostDeleteSuccess(post);
            log.info("Dify文档删除成功，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId());
            
        } catch (Exception e) {
            log.error("删除Dify文档失败，文章ID:{} 标题:{} difyDocumentId:{}", postId, post.getTitle(), post.getDifyDocumentId(), e);
            applyPostDeleteFailure(postId, e.getMessage());
        }
    }

    @Override
    @Async
    public void syncPost(Long postId) {
        Post post = loadPostWithCategory(postId);
        if (post == null) {
            log.warn("文章未找到,文章ID: {}", postId);
            return;
        }
        
        // 检查是否满足同步条件
        if (!shouldSyncPost(post)) {
            log.info("文章不满足同步条件，跳过同步，文章ID:{} 标题:{}", postId, post.getTitle());
            return;
        }
        
        String difyDocumentId = post.getDifyDocumentId();
        SyncStatus syncStatus = post.getSyncStatus();
        
        // 智能判断操作类型
        if (difyDocumentId == null || difyDocumentId.isEmpty() || syncStatus == SyncStatus.FAILED) {
            // 创建（包括重试失败的情况）
            createPostToDify(postId);
        } else if (syncStatus == SyncStatus.SYNCED) {
            // 更新
            updatePostInDify(postId);
        } else {
            // 其他状态（如UNSYNCED），尝试创建
            createPostToDify(postId);
        }
    }


    // ========== Post辅助方法 ==========
    
    /**
     * 在事务中加载Post及其关联的Category，避免懒加载问题
     */
    @Transactional(readOnly = true)
    public Post loadPostWithCategory(Long postId) {
        return postRepository.findByIdWithCategory(postId).orElse(null);
    }
    
    /**
     * 检查文章是否应该同步到Dify
     */
    private boolean shouldSyncPost(Post post) {
        // 检查文章状态
        if (post.getStatus() != PostStatus.PUBLISHED) {
            return false;
        }
        
        // 检查可见性
        if (post.getVisibility() != Visibility.PUBLIC) {
            return false;
        }
        
        // 检查是否有密码保护
        if (post.getPassword() != null && !post.getPassword().isEmpty()) {
            return false;
        }
        
        // 检查是否有分类
        if (post.getCategory() == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取文章分类对应的知识库ID
     */
    private String getDatasetIdForPost(Post post) {
        if (post.getCategory() == null) {
            return null;
        }
        
        Category category = post.getCategory();
        return category.getDifyDatasetId();
    }
    
    /**
     * 构建文章内容
     */
    private String buildPostContent(Post post) {
        StringBuilder content = new StringBuilder();
        
        content.append("标题: ").append(post.getTitle()).append("\n\n");
        
        if (post.getCategory() != null) {
            content.append("分类: ").append(post.getCategory().getName()).append("\n\n");
        }
        
        if (post.getExcerpt() != null && !post.getExcerpt().isEmpty()) {
            content.append("摘要: ").append(post.getExcerpt()).append("\n\n");
        }
        
        content.append("内容: ").append(post.getContent()).append("\n\n");
        
        if (post.getMetaKeywords() != null && !post.getMetaKeywords().isEmpty()) {
            content.append("标签: ").append(post.getMetaKeywords()).append("\n\n");
        }
        
        if (post.getPublishedAt() != null) {
            content.append("发布时间: ").append(post.getPublishedAt()).append("\n\n");
        }
        
        return content.toString();
    }
    
    /**
     * 应用文章同步成功
     */
    @Transactional
    protected void applyPostUpsertSuccess(Post post, String difyDocumentId) {
        post.setDifyDocumentId(difyDocumentId);
        post.setSyncStatus(SyncStatus.SYNCED);
        post.setSyncError(null);
        postRepository.save(post);
    }
    
    /**
     * 应用文章同步失败
     */
    @Transactional
    protected void applyPostUpsertFailure(Long postId, String errorMsg) {
        postRepository.findById(postId).ifPresent(p -> {
            p.setSyncStatus(SyncStatus.UNSYNCED);
            p.setSyncError(truncate(errorMsg));
            postRepository.save(p);
        });
    }
    
    /**
     * 应用文章删除成功
     */
    @Transactional
    protected void applyPostDeleteSuccess(Post post) {
        post.setDifyDocumentId(null);
        post.setSyncStatus(SyncStatus.UNSYNCED);
        post.setSyncError(null);
        postRepository.save(post);
    }
    
    /**
     * 应用文章删除失败
     */
    @Transactional
    protected void applyPostDeleteFailure(Long postId, String errorMsg) {
        postRepository.findById(postId).ifPresent(p -> {
            p.setSyncError(truncate(errorMsg));
            postRepository.save(p);
        });
    }

}


