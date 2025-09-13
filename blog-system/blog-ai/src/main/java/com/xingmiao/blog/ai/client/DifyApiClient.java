package com.xingmiao.blog.ai.client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Dify API 客户端
 * 用于与 Dify 知识库进行文档/知识库同步操作
 */
@Slf4j
@Component
public class DifyApiClient {

    private final String baseUrl;
    private final String apiKey;
    private final String workflowApiKey;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DifyApiClient(@Value("${dify.base-url}") String baseUrl,
                         @Value("${dify.api-key}") String apiKey,
                         @Value("${dify.workflowApiKey:}") String workflowApiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.workflowApiKey = workflowApiKey;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        log.info("DifyApiClient 初始化完成，baseUrl: {}", baseUrl);

        if (workflowApiKey == null || workflowApiKey.isEmpty()) {
            log.warn("未配置 dify.workflowApiKey，工作流调用将回退使用 dify.api-key（可能鉴权失败）");
        }
    }

    /**
     * 创建知识库
     * @param name 知识库名称
     * @param description 知识库描述
     * @return API响应结果
     */
    public String createDataset(String name, String description) throws IOException {
        String url = baseUrl + "/v1/datasets";

        log.info("开始创建 Dify 知识库，名称: {}", name);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        if (description != null && !description.isEmpty()) {
            requestBody.put("description", description);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        log.debug("创建知识库请求体: {}", jsonBody);

        // 创建HTTP请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 知识库创建完成，状态码: {}, 名称: {}", statusCode, name);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 知识库创建成功，名称: {}", name);
            } else {
                log.error("Dify 知识库创建失败，状态码: {}, 名称: {}, 响应: {}", statusCode, name, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 知识库创建异常，名称: {}, 错误: {}", name, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除知识库
     * @param datasetId 知识库ID
     * @return API响应结果
     */
    public String deleteDataset(String datasetId) throws IOException {
        String url = baseUrl + "/v1/datasets/" + datasetId;

        log.info("开始删除 Dify 知识库，datasetId: {}", datasetId);

        // 创建HTTP请求
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + apiKey);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 知识库删除完成，状态码: {}, datasetId: {}", statusCode, datasetId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 知识库删除成功，datasetId: {}", datasetId);
            } else {
                log.error("Dify 知识库删除失败，状态码: {}, datasetId: {}, 响应: {}", statusCode, datasetId, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 知识库删除异常，datasetId: {}, 错误: {}", datasetId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新知识库
     * @param datasetId 知识库ID
     * @param name 知识库名称(可选)
     * @param description 知识库描述(可选)
     * @return API响应结果
     */
    public String updateDataset(String datasetId, String name, String description) throws IOException {
        String url = baseUrl + "/v1/datasets/" + datasetId;

        log.info("开始更新 Dify 知识库，datasetId: {}", datasetId);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        if (name != null && !name.isEmpty()) {
            requestBody.put("name", name);
        }
        if (description != null && !description.isEmpty()) {
            requestBody.put("description", description);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        log.debug("更新知识库请求体: {}", jsonBody);

        // 创建HTTP请求
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setHeader("Authorization", "Bearer " + apiKey);
        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPatch)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 知识库更新完成，状态码: {}, datasetId: {}", statusCode, datasetId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 知识库更新成功，datasetId: {}", datasetId);
            } else {
                log.error("Dify 知识库更新失败，状态码: {}, datasetId: {}, 响应: {}", statusCode, datasetId, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 知识库更新异常，datasetId: {}, 错误: {}", datasetId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 通过文件创建知识库
     * @param datasetId 知识库ID
     * @param file 知识库名称(可选)
     * @param dataConfig 文档向量化等操作配置(可选)
     * @return API响应结果
     */
    public String createDocumentByFile(String datasetId, File file,Map<String,Object> dataConfig) throws Exception{
        String url = baseUrl + "/v1/datasets/" + datasetId + "/document/create-by-file";
        String dataJson = (dataConfig == null) ? "{}" : objectMapper.writeValueAsString(dataConfig);

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", "Bearer " + apiKey);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("data", dataJson, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
        builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
        post.setEntity(builder.build());

        log.info("POST {}", url);
        log.debug("data={}", dataJson);

        try (CloseableHttpClient http = HttpClients.createDefault();
             CloseableHttpResponse resp = http.execute(post)) {
            String body = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
            int code = resp.getStatusLine().getStatusCode();
            log.info("Dify create-by-file status={}, bodyLength={}", code, body.length());
            if (code < 200 || code >= 300) {
                throw new RuntimeException("Dify 通过文件创建文档失败: " + body);
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode doc = root.path("document");
            String difyDocumentId = doc.path("id").asText(null);
            if (difyDocumentId == null) {
                throw new RuntimeException("Dify 响应中缺少 document.id: " + body);
            }
            return difyDocumentId;
        }
    }

    /**
     * 通过文本更新 Dify 知识库中的文档
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @param name 文档名称（可选）
     * @param text 文档内容（可选）
     * @return API响应结果
     */
    public String updateDocumentByText(String datasetId, String documentId, String name, String text) throws IOException {
        String url = baseUrl + "/v1/datasets/" + datasetId + "/documents/" + documentId + "/update-by-text";

        log.info("开始通过文本更新 Dify 文档，datasetId: {}, documentId: {}, url: {}", datasetId, documentId, url);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        if (name != null && !name.isEmpty()) {
            requestBody.put("name", name);
        }
        if (text != null && !text.isEmpty()) {
            requestBody.put("text", text);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        log.debug("更新文档请求体: {}", jsonBody);

        // 创建HTTP请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 文档更新完成，状态码: {}, documentId: {}", statusCode, documentId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 文档更新成功，documentId: {}", documentId);
            } else {
                log.error("Dify 文档更新失败，状态码: {}, documentId: {}, 响应: {}", statusCode, documentId, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 文档更新异常，documentId: {}, 错误: {}", documentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除 Dify 知识库中的文档
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @return API响应结果
     */
    public String deleteDocument(String datasetId, String documentId) throws IOException {
        String url = baseUrl + "/v1/datasets/" + datasetId + "/documents/" + documentId;

        log.info("开始删除 Dify 文档，datasetId: {}, documentId: {}, url: {}", datasetId, documentId, url);

        // 创建HTTP请求
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Authorization", "Bearer " + apiKey);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 文档删除完成，状态码: {}, documentId: {}", statusCode, documentId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 文档删除成功，documentId: {}", documentId);
            } else {
                log.error("Dify 文档删除失败，状态码: {}, documentId: {}, 响应: {}", statusCode, documentId, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 文档删除异常，documentId: {}, 错误: {}", documentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 通过文件更新 Dify 知识库中的文档
     * @param datasetId 知识库ID
     * @param documentId 文档ID
     * @param file 需要上传的文件
     * @return 更新是否成功
     */
    public boolean updateDocumentByFile(String datasetId, String documentId, File file) throws Exception {
        String url = baseUrl + "/v1/datasets/" + datasetId + "/documents/" + documentId + "/update-by-file";

        log.info("开始通过文件更新 Dify 文档，datasetId: {}, documentId: {}, url: {}", datasetId, documentId, url);

        // 构建正确的配置数据，按照API文档格式
        Map<String, Object> dataConfig = new HashMap<>();
        dataConfig.put("indexing_technique", "high_quality");

        // 构建process_rule，注意嵌套结构
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

        String dataJson = objectMapper.writeValueAsString(dataConfig);

        log.info("构建的data配置: {}", dataConfig);
        log.info("序列化后的JSON字符串: {}", dataJson);

        // 创建HTTP请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);

        // 构建multipart请求体
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("data", dataJson, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
        builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
        httpPost.setEntity(builder.build());

        log.info("POST {}", url);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 文档文件更新完成，状态码: {}, documentId: {}", statusCode, documentId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 文档文件更新成功，documentId: {}", documentId);
                return true;
            } else {
                log.error("Dify 文档文件更新失败，状态码: {}, documentId: {}, 响应: {}", statusCode, documentId, responseBody);
                return false;
            }
        } catch (Exception e) {
            log.error("Dify 文档文件更新异常，documentId: {}, 错误: {}", documentId, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 关闭HTTP客户端
     */
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            log.info("DifyApiClient HTTP客户端已关闭");
        }
    }




    /**
     * 调用Dify 工作流
     * @param workflowId 工作流ID
     * @param inputs 输入参数
     * @return 工作流执行结果
     */
    public String callWorkflow(String workflowId, Map<String,Object> inputs) throws IOException{
        // 按官方文档：POST /v1/workflows/run，不再在路径中携带 workflowId（API-Key 绑定应用）
        String url = baseUrl + "/v1/workflows/run";

        log.info("开始调用Dify工作流(官方 /v1/workflows/run)，workflowId(仅日志): {}",workflowId);


        //构建请求体
        Map<String,Object> requestBody = new HashMap<>();
        Map<String,Object> safeInputs = (inputs == null) ? new HashMap<>() : inputs;
        requestBody.put("inputs", safeInputs);
        // 官方必填：response_mode 与 user
        requestBody.put("response_mode", "blocking");
        requestBody.put("user", "api-user");
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        log.debug("请求体构建完成,jsonBody: {}",jsonBody);


        //创建http请求
        HttpPost httpPost = new HttpPost(url);
        // 工作流优先使用 workflowApiKey
        String keyToUse = (workflowApiKey == null || workflowApiKey.isEmpty()) ? apiKey : workflowApiKey;
        httpPost.setHeader("Authorization","Bearer " + keyToUse);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(new StringEntity(jsonBody,StandardCharsets.UTF_8));

        //执行请求
        try(CloseableHttpResponse response = httpClient.execute(httpPost)){
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 工作流调用完成, 状态码: {}, url: {}",statusCode,url);
            log.debug("Dify 响应体: {}",responseBody);

            if (statusCode >=200 && statusCode <300){
                log.info("Dify 工作流调用成功");
                return responseBody;
            } else {
                // 非 2xx 明确抛出异常，避免上层将 HTML/错误信息当作 JSON 解析
                String message = String.format("Dify 工作流调用失败, 状态码:%d, url:%s, 响应:%s", statusCode, url, responseBody);
                log.error(message);
                throw new IOException(message);
            }
        }catch (Exception e){
            log.error("Dify工作流调用异常, 错误:{}",e.getMessage());
            throw e;
        }
    }


    /**
     * 获取工作流执行结果
     * @param runId 任务ID
     */
    public String getWorkflowResult(String runId) throws IOException {
        // 官方文档：GET /v1/workflows/run/:workflow_id 这里的参数是 workflow_run_id
        String url = baseUrl + "/v1/workflows/run/" + runId;

        log.info("开始获取 Dify 工作流结果，workflow_run_id: {}", runId);

        // 创建HTTP请求
        HttpGet httpGet = new HttpGet(url);
        String keyToUseForGet = (workflowApiKey == null || workflowApiKey.isEmpty()) ? apiKey : workflowApiKey;
        httpGet.setHeader("Authorization", "Bearer " + keyToUseForGet);

        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("Dify 工作流结果获取完成，状态码: {}, runId: {}", statusCode, runId);
            log.debug("Dify 响应体: {}", responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Dify 工作流结果获取成功，runId: {}", runId);
            } else {
                log.error("Dify 工作流结果获取失败，状态码: {}, runId: {}, 响应: {}", statusCode, runId, responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            log.error("Dify 工作流结果获取异常，runId: {}, 错误: {}", runId, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 同步调用工作流并等待结果
     * @param workflowId 工作流ID
     * @param inputs 输入参数
     * @param timeoutSeconds 超时时间（秒）
     * @return 工作流执行结果
     */
    public String callWorkflowSync(String workflowId, Map<String, Object> inputs, int timeoutSeconds) throws Exception {
        log.info("开始同步调用 Dify 工作流，workflowId: {}, 超时时间: {}秒", workflowId, timeoutSeconds);

        // 1. 启动工作流
        String initResponse = callWorkflow(workflowId, inputs);
        JsonNode root = objectMapper.readTree(initResponse);
        // 若阻塞模式已直接返回最终结构（常见：data.outputs.text），直接返回
        JsonNode directOutputs = root.path("data").path("outputs");
        String directText = directOutputs.path("text").asText(null);
        String directStatus = root.path("data").path("status").asText(null);
        if ((directText != null && !directText.isEmpty()) || "succeeded".equals(directStatus) || "failed".equals(directStatus)) {
            log.info("阻塞模式POST已返回最终结果，跳过轮询，status:{}", directStatus);
            return initResponse;
        }
        // 官方返回 blocking 模式下包含 workflow_run_id 与 task_id
        String runId = root.path("workflow_run_id").asText(null);
        if (runId == null || runId.isEmpty()) {
            // 兼容某些实现把 runId 放在 data.id
            runId = root.path("data").path("id").asText(null);
        }
        String taskId = root.path("task_id").asText(null);

        if (runId == null) {
            throw new RuntimeException("未获取到 workflow_run_id: " + initResponse);
        }

        log.info("工作流启动成功，workflow_run_id: {}, taskId: {}", runId, taskId);

        // 2. 轮询等待结果
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            String result = getWorkflowResult(runId);
            JsonNode resultNode = objectMapper.readTree(result);

            String status = resultNode.path("status").asText();

            if ("completed".equals(status) || "succeeded".equals(status)) {
                log.info("工作流执行完成，taskId: {}", taskId);
                return result;
            } else if ("failed".equals(status)) {
                String error = resultNode.path("error").asText("未知错误");
                throw new RuntimeException("工作流执行失败: " + error);
            }

            // 等待1秒后重试
            Thread.sleep(1000);
        }

        throw new RuntimeException("工作流执行超时，taskId: " + taskId);
    }


}
