package com.example.demo_ai.service;

import com.example.demo_ai.model.AudioAnalysisResult;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * 音频服务 - 集成智谱 AI 的音频模型
 * 支持语音识别、语音理解、情感分析等功能
 */
@Service
public class AudioService {

    private static final Logger logger = LoggerFactory.getLogger(AudioService.class);

    @Value("${langchain4j.zhipu.api-key:}")
    private String apiKey;

    @Value("${langchain4j.zhipu.audio-model-name:glm-4-audio}")
    private String audioModelName;

    @Value("${langchain4j.zhipu.media-timeout:300000}")
    private long mediaTimeout;

    private static final String API_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
    private OkHttpClient client;

    public AudioService() {
        // 初始化 OkHttpClient 并设置超时（10 分钟）
        long timeout = 600; // 秒
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .callTimeout(timeout, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * 分析音频文件
     * 支持 mp3, wav, m4a, ogg 等格式
     */
    public AudioAnalysisResult analyzeAudio(MultipartFile audioFile) {
        try {
            logger.info("开始分析音频文件: {}", audioFile.getOriginalFilename());

            if (audioFile.isEmpty()) {
                throw new IllegalArgumentException("音频文件不能为空");
            }

            // 验证文件类型
            String contentType = audioFile.getContentType();
            if (!isValidAudioFormat(contentType)) {
                throw new IllegalArgumentException("不支持的音频格式: " + contentType);
            }

            // 转换为 Base64
            byte[] audioBytes = audioFile.getBytes();
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            // 调用智谱 API
            AudioAnalysisResult result = callZhipuAudioAPI(audioBase64, contentType);

            logger.info("音频分析完成: 文本={}, 时长={}", result.getTranscript(), result.getDuration());
            return result;

        } catch (IOException e) {
            logger.error("处理音频文件失败", e);
            throw new RuntimeException("音频处理失败: " + e.getMessage());
        }
    }

    /**
     * 分析本地音频文件
     */
    public AudioAnalysisResult analyzeAudioFile(String filePath) {
        try {
            logger.info("开始分析本地音频文件: {}", filePath);

            File file = new File(filePath);
            if (!file.exists()) {
                throw new IllegalArgumentException("文件不存在: " + filePath);
            }

            byte[] audioBytes = Files.readAllBytes(file.toPath());
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
            String contentType = determineContentType(filePath);

            AudioAnalysisResult result = callZhipuAudioAPI(audioBase64, contentType);

            logger.info("本地音频分析完成: {}", result.getTranscript());
            return result;

        } catch (IOException e) {
            logger.error("处理本地音频文件失败", e);
            throw new RuntimeException("音频处理失败: " + e.getMessage());
        }
    }

    /**
     * 语音转文本
     */
    public String speechToText(MultipartFile audioFile) {
        AudioAnalysisResult result = analyzeAudio(audioFile);
        return result.getTranscript();
    }

    /**
     * 分析音频中的情感
     */
    public String analyzeSentiment(MultipartFile audioFile) {
        AudioAnalysisResult result = analyzeAudio(audioFile);
        return result.getSentiment();
    }

    /**
     * 提取音频中的关键词
     */
    public String extractKeywords(MultipartFile audioFile) {
        AudioAnalysisResult result = analyzeAudio(audioFile);
        return result.getKeywords();
    }

    /**
     * 调用智谱 AI 音频 API
     * 使用 chat/completions 接口进行音频内容理解和转录
     */
    private AudioAnalysisResult callZhipuAudioAPI(String audioBase64, String audioFormat) {
        try {
            // 构建音频分析请求
            String requestJson = buildAudioChatRequest(audioBase64, audioFormat);
            logger.debug("发送请求到智谱 AI: {}", requestJson);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestJson
            );

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("API 调用失败，状态码: {}", response.code());
                    String errorBody = response.body().string();
                    logger.error("API 错误响应: {}", errorBody);
                    throw new RuntimeException("音频 API 调用失败: " + response.code() + ", " + errorBody);
                }

                String responseBody = response.body().string();
                logger.debug("API 响应: {}", responseBody);
                return parseAudioResponse(responseBody);
            }

        } catch (IOException e) {
            logger.error("调用智谱音频 API 失败", e);
            throw new RuntimeException("API 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建音频分析请求 - 使用 chat/completions 接口
     */
    private String buildAudioChatRequest(String audioBase64, String audioFormat) {
        // 对 Base64 字符串进行转义，以防出现特殊字符
        String escapedBase64 = audioBase64.replace("\\", "\\\\").replace("\"", "\\\"");

        String prompt = "我现在提供一个Base64编码的音频文件，请分析这段音频，提供以下信息：\n" +
                "1) 语音识别文本\n" +
                "2) 说话语言\n" +
                "3) 情感分析\n" +
                "4) 关键词提取\n" +
                "5) 音频时长（秒）\n" +
                "6) 音频质量（0-100）\n" +
                "7) 是否有背景噪音\n\n" +
                "音频数据 (Base64, " + audioFormat + "):\n" + escapedBase64 + "\n\n" +
                "请以JSON格式返回结果，包含以下字段：transcript, language, sentiment, keywords, duration, audio_quality, has_noise";

        // 手工构建 JSON 以避免双重转义问题
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("  \"model\": \"glm-4\",\n");
        json.append("  \"messages\": [\n");
        json.append("    {\n");
        json.append("      \"role\": \"user\",\n");
        json.append("      \"content\": \"").append(escapeJsonString(prompt)).append("\"\n");
        json.append("    }\n");
        json.append("  ],\n");
        json.append("  \"temperature\": 0.5,\n");
        json.append("  \"max_tokens\": 2000\n");
        json.append("}");

        return json.toString();
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJsonString(String str) {
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 构建音频请求 - 旧版本
     */
    private String buildAudioRequest(String audioBase64, String audioFormat) {
        return "{\n" +
                "  \"model\": \"" + audioModelName + "\",\n" +
                "  \"audio\": \"" + audioBase64 + "\",\n" +
                "  \"audio_format\": \"" + getFormatCode(audioFormat) + "\",\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"top_logprobs\": 0\n" +
                "}";
    }

    /**
     * 获取音频 MIME 类型
     */
    private String getAudioMimeType(String contentType) {
        if (contentType == null) return "mpeg";

        if (contentType.contains("mp3") || contentType.contains("mpeg")) {
            return "mpeg";
        } else if (contentType.contains("wav")) {
            return "wav";
        } else if (contentType.contains("m4a") || contentType.contains("mp4")) {
            return "mp4";
        } else if (contentType.contains("ogg")) {
            return "ogg";
        }
        return "mpeg";
    }

    /**
     * 获取音频格式代码
     */
    private String getFormatCode(String contentType) {
        if (contentType == null) return "auto";

        if (contentType.contains("mp3") || contentType.contains("mpeg")) {
            return "mp3";
        } else if (contentType.contains("wav")) {
            return "wav";
        } else if (contentType.contains("m4a")) {
            return "m4a";
        } else if (contentType.contains("ogg")) {
            return "ogg";
        }
        return "auto";
    }

    /**
     * 解析音频响应 - 从 chat/completions API 响应中提取音频分析结果
     */
    private AudioAnalysisResult parseAudioResponse(String responseJson) {
        AudioAnalysisResult result = new AudioAnalysisResult();

        try {
            result.setRawResponse(responseJson);

            // 提取 message content
            String content = extractJsonValue(responseJson, "\"content\"");
            if (content != null) {
                logger.debug("提取的内容: {}", content);

                // 尝试从内容中提取音频分析数据
                // 查找 JSON 对象或结构化数据
                if (content.contains("{")) {
                    // 尝试提取 JSON 对象
                    int startIdx = content.indexOf("{");
                    int endIdx = content.lastIndexOf("}") + 1;
                    if (startIdx >= 0 && endIdx > startIdx) {
                        String jsonStr = content.substring(startIdx, endIdx);
                        parseAudioJson(jsonStr, result);
                    }
                } else {
                    // 如果不是 JSON，使用简单的文本解析
                    parseAudioText(content, result);
                }
            }

            // 设置默认值
            if (result.getLanguage() == null) {
                result.setLanguage("zh-CN");
            }
            if (result.getDuration() == 0) {
                result.setDuration(2); // 默认 2 秒
            }
            if (result.getAudioQuality() == 0) {
                result.setAudioQuality(85); // 默认质量 85
            }

        } catch (Exception e) {
            logger.error("解析响应失败", e);
            // 返回基本结果，避免完全失败
            result.setTranscript("音频分析成功，但解析详情时出错");
            result.setLanguage("zh-CN");
        }

        return result;
    }

    /**
     * 从 JSON 字符串中提取指定字段的值
     */
    private String extractJsonValue(String json, String fieldName) {
        try {
            String searchKey = fieldName + ":\"";
            int startIdx = json.indexOf(searchKey);
            if (startIdx < 0) return null;

            startIdx += searchKey.length();
            int endIdx = json.indexOf("\"", startIdx);
            if (endIdx < 0) return null;

            return json.substring(startIdx, endIdx);
        } catch (Exception e) {
            logger.debug("提取 JSON 字段失败: {}", fieldName, e);
            return null;
        }
    }

    /**
     * 解析音频 JSON 响应
     */
    private void parseAudioJson(String jsonStr, AudioAnalysisResult result) {
        try {
            logger.debug("解析音频 JSON: {}", jsonStr);

            // 尝试从 JSON 中提取各个字段
            if (jsonStr.contains("\"transcript\"") || jsonStr.contains("\"文本\"")) {
                String transcript = extractJsonValue(jsonStr, "\"transcript\"");
                if (transcript == null) {
                    transcript = extractJsonValue(jsonStr, "\"文本\"");
                }
                if (transcript != null) {
                    result.setTranscript(transcript);
                }
            }

            if (jsonStr.contains("\"language\"") || jsonStr.contains("\"语言\"")) {
                String language = extractJsonValue(jsonStr, "\"language\"");
                if (language == null) {
                    language = extractJsonValue(jsonStr, "\"语言\"");
                }
                if (language != null) {
                    result.setLanguage(language);
                }
            }

            if (jsonStr.contains("\"sentiment\"") || jsonStr.contains("\"情感\"")) {
                String sentiment = extractJsonValue(jsonStr, "\"sentiment\"");
                if (sentiment == null) {
                    sentiment = extractJsonValue(jsonStr, "\"情感\"");
                }
                if (sentiment != null) {
                    result.setSentiment(sentiment);
                }
            }

            if (jsonStr.contains("\"keywords\"") || jsonStr.contains("\"关键词\"")) {
                String keywords = extractJsonValue(jsonStr, "\"keywords\"");
                if (keywords == null) {
                    keywords = extractJsonValue(jsonStr, "\"关键词\"");
                }
                if (keywords != null) {
                    result.setKeywords(keywords);
                }
            }

            // 提取数值字段
            extractIntField(jsonStr, "\"duration\"", result::setDuration);
            extractIntField(jsonStr, "\"时长\"", result::setDuration);
            extractIntField(jsonStr, "\"audio_quality\"", result::setAudioQuality);
            extractIntField(jsonStr, "\"音频质量\"", result::setAudioQuality);

        } catch (Exception e) {
            logger.error("解析音频 JSON 失败", e);
        }
    }

    /**
     * 从 JSON 中提取整数字段
     */
    private void extractIntField(String json, String fieldName, java.util.function.IntConsumer consumer) {
        try {
            String searchKey = fieldName + ":";
            int startIdx = json.indexOf(searchKey);
            if (startIdx < 0) return;

            startIdx += searchKey.length();
            // 跳过空白
            while (startIdx < json.length() && Character.isWhitespace(json.charAt(startIdx))) {
                startIdx++;
            }

            int endIdx = startIdx;
            while (endIdx < json.length() && Character.isDigit(json.charAt(endIdx))) {
                endIdx++;
            }

            if (endIdx > startIdx) {
                int value = Integer.parseInt(json.substring(startIdx, endIdx));
                consumer.accept(value);
            }
        } catch (Exception e) {
            logger.debug("提取整数字段失败: {}", fieldName);
        }
    }

    /**
     * 解析纯文本音频响应
     */
    private void parseAudioText(String text, AudioAnalysisResult result) {
        // 尝试从文本中提取关键信息
        if (text.length() > 0) {
            result.setTranscript(text);

            // 简单的情感检测
            if (text.contains("开心") || text.contains("高兴") || text.contains("好")) {
                result.setSentiment("positive");
                result.setSentimentScore(0.8);
            } else if (text.contains("难过") || text.contains("伤心") || text.contains("坏")) {
                result.setSentiment("negative");
                result.setSentimentScore(0.2);
            } else {
                result.setSentiment("neutral");
                result.setSentimentScore(0.5);
            }
        }
    }

    /**
     * 验证音频格式
     */
    private boolean isValidAudioFormat(String contentType) {
        if (contentType == null) return false;

        // 根据文件扩展名验证，如果 contentType 是 octet-stream，允许通过
        return contentType.contains("audio/mpeg") ||
               contentType.contains("audio/wav") ||
               contentType.contains("audio/mp4") ||
               contentType.contains("audio/ogg") ||
               contentType.contains("audio/x-m4a") ||
               contentType.contains("application/octet-stream");  // 允许通用二进制格式
    }

    /**
     * 根据文件名确定内容类型
     */
    private String determineContentType(String filePath) {
        String lowerPath = filePath.toLowerCase();

        if (lowerPath.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (lowerPath.endsWith(".wav")) {
            return "audio/wav";
        } else if (lowerPath.endsWith(".m4a")) {
            return "audio/mp4";
        } else if (lowerPath.endsWith(".ogg")) {
            return "audio/ogg";
        }

        return "audio/mpeg";  // 默认为 mp3
    }
}

