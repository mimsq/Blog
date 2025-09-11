package com.xingmiao.blog.app.service.impl;

import com.xingmiao.blog.app.repository.CategoryRepository;
import com.xingmiao.blog.app.service.DifySyncService;
import com.xingmiao.blog.common.domain.entity.Category;
import com.xingmiao.blog.common.domain.enums.SyncStatus;
import com.xingmiao.blog.ai.client.DifyApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DifySyncServiceImpl implements DifySyncService {

    private final CategoryRepository categoryRepository;
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
                    String response = difyApiClient.createDataset(category.getName(), null);
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
                    difyApiClient.updateDataset(datasetId, category.getName(), null);
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
           //TODO，后续在分类添加选填字段"description"后再同步到dify
           String response = difyApiClient.createDataset(KnowledgeBaseName,null);

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
                    String response = difyApiClient.createDataset(category.getName(), null);
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
                    difyApiClient.updateDataset(datasetId, category.getName(), null);
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


}


