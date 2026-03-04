package com.example.demo_ai.controller;

import com.example.demo_ai.model.AudioAnalysisResult;
import com.example.demo_ai.model.VideoAnalysisResult;
import com.example.demo_ai.service.AudioService;
import com.example.demo_ai.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 媒体控制器 - 处理音频和视频分析请求
 */
@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    @Autowired
    private AudioService audioService;

    @Autowired
    private VideoService videoService;

    /**
     * 分析音频文件
     * POST /api/media/audio/analyze
     */
    @PostMapping("/audio/analyze")
    public ResponseEntity<?> analyzeAudio(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("接收音频分析请求: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", "error");
                errorMap.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(errorMap);
            }

            AudioAnalysisResult result = audioService.analyzeAudio(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "音频分析完成");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("参数错误: {}", e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        } catch (Exception e) {
            logger.error("音频分析失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "音频分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 语音转文本
     * POST /api/media/audio/speech-to-text
     */
    @PostMapping("/audio/speech-to-text")
    public ResponseEntity<?> speechToText(@RequestParam("file") MultipartFile file) {
        try {
            String transcript = audioService.speechToText(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "语音转文本完成");
            response.put("transcript", transcript);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("语音转文本失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "语音转文本失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 分析音频中的情感
     * POST /api/media/audio/sentiment
     */
    @PostMapping("/audio/sentiment")
    public ResponseEntity<?> analyzeSentiment(@RequestParam("file") MultipartFile file) {
        try {
            String sentiment = audioService.analyzeSentiment(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "情感分析完成");
            response.put("sentiment", sentiment);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("情感分析失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "情感分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 分析视频文件
     * POST /api/media/video/analyze
     */
    @PostMapping("/video/analyze")
    public ResponseEntity<?> analyzeVideo(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("接收视频分析请求: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", "error");
                errorMap.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(errorMap);
            }

            VideoAnalysisResult result = videoService.analyzeVideo(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "视频分析完成");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("参数错误: {}", e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        } catch (Exception e) {
            logger.error("视频分析失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "视频分析失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 检测视频中的物体
     * POST /api/media/video/detect-objects
     */
    @PostMapping("/video/detect-objects")
    public ResponseEntity<?> detectObjects(@RequestParam("file") MultipartFile file) {
        try {
            String objects = videoService.detectObjects(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "物体检测完成");
            response.put("objects", objects);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("物体检测失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "物体检测失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 提取视频中的文字
     * POST /api/media/video/extract-text
     */
    @PostMapping("/video/extract-text")
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String text = videoService.extractText(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "文字提取完成");
            response.put("text", text);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文字提取失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "文字提取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 提取视频中的音频转录
     * POST /api/media/video/extract-audio
     */
    @PostMapping("/video/extract-audio")
    public ResponseEntity<?> extractAudio(@RequestParam("file") MultipartFile file) {
        try {
            String transcript = videoService.extractAudioTranscript(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "音频提取完成");
            response.put("transcript", transcript);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("音频提取失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "音频提取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 获取视频场景描述
     * POST /api/media/video/scene-description
     */
    @PostMapping("/video/scene-description")
    public ResponseEntity<?> getSceneDescription(@RequestParam("file") MultipartFile file) {
        try {
            String description = videoService.getSceneDescription(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "场景描述获取完成");
            response.put("description", description);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("场景描述获取失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "场景描述获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 检测视频中的人脸
     * POST /api/media/video/detect-faces
     */
    @PostMapping("/video/detect-faces")
    public ResponseEntity<?> detectFaces(@RequestParam("file") MultipartFile file) {
        try {
            int faceCount = videoService.detectFaces(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "人脸检测完成");
            response.put("faceCount", faceCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("人脸检测失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "人脸检测失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 获取视频质量评分
     * POST /api/media/video/quality-score
     */
    @PostMapping("/video/quality-score")
    public ResponseEntity<?> getQualityScore(@RequestParam("file") MultipartFile file) {
        try {
            int score = videoService.getQualityScore(file);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "质量评分获取完成");
            response.put("score", score);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("质量评分获取失败", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "质量评分获取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    /**
     * 获取媒体处理能力信息
     * GET /api/media/capabilities
     */
    @GetMapping("/capabilities")
    public ResponseEntity<?> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();

        // 音频能力
        Map<String, Object> audioCapabilities = new HashMap<>();
        audioCapabilities.put("formats", new String[]{"mp3", "wav", "m4a", "ogg"});
        audioCapabilities.put("features", new String[]{
                "语音识别 (Speech-to-Text)",
                "情感分析",
                "关键词提取",
                "语言识别"
        });
        audioCapabilities.put("maxSize", "100MB");

        // 视频能力
        Map<String, Object> videoCapabilities = new HashMap<>();
        videoCapabilities.put("formats", new String[]{"mp4", "mov", "avi", "mkv", "webm"});
        videoCapabilities.put("features", new String[]{
                "视频理解",
                "物体检测",
                "人脸检测",
                "文字提取",
                "场景识别",
                "音频转录",
                "质量评分"
        });
        videoCapabilities.put("maxSize", "500MB");

        capabilities.put("audio", audioCapabilities);
        capabilities.put("video", videoCapabilities);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "媒体处理能力");
        response.put("capabilities", capabilities);
        return ResponseEntity.ok(response);
    }
}

