package com.xingmiao.blog.app.service;



import com.xingmiao.blog.common.domain.entity.Post;

import java.io.File;
public interface DifySyncService {
    /**
     * 更新 Dify 知识库中的文章
     * @param post 要更新的文档
     * @return 是否更新成功
     */
    boolean updateDocumentInDify(Post post);

    /**
     * 从 Dify 知识库中删除文档
     * @param document 要删除的文档
     * @return 是否删除成功
     */
    boolean removeDocumentFromDify(Post document);

    /**
     * 通过文件在Dify知识库创建文档
     *
     * @param datasetId 知识库ID
     * @param file 要上传的文件
     * @return 文档在Dify中的唯一标识ID
     * @throws Exception 当文件上传失败或API调用出错时抛出
     */
    String createDocumentByFile(String datasetId, File file) throws Exception;


    /**
     * 通过文件在Dify知识库更新文档
     *
     * @param datasetId 知识库ID
     * @param documentId Dify文档ID
     * @param file 要上传的文件
     * @return 是否更新成功
     * @throws Exception 当文件上传失败或API调用出错时抛出
     */
    boolean updateDocumentByFile(String datasetId, String documentId, File file) throws Exception;
}
