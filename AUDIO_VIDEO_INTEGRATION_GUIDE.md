# 智谱音视频模型集成指南

## 🎯 概述

本项目集成了智谱 AI 的音频和视频模型，提供了强大的多媒体处理能力。通过 RESTful API，你可以轻松实现语音识别、视频理解、物体检测等功能。

## 🚀 快速开始

### 1. 环境配置

在 `application.yml` 中配置智谱 API Key：

```yaml
langchain4j:
  zhipu:
    api-key: ${ZHIPU_API_KEY:your-api-key-here}
    audio-model-name: glm-4-audio
    video-model-name: glm-4-video
    media-timeout: 300000  # 5分钟超时
```

或者通过环境变量设置：

```bash
export ZHIPU_API_KEY=your-api-key
export ZHIPU_AUDIO_MODEL=glm-4-audio
export ZHIPU_VIDEO_MODEL=glm-4-video
```

### 2. 启动应用

```bash
cd demo-ai
mvn spring-boot:run
```

## 📊 API 文档

### 一、音频处理 API

#### 1.1 分析音频文件

**端点：** `POST /api/media/audio/analyze`

**请求：**
```bash
curl -X POST \
  -F "file=@audio.mp3" \
  http://localhost:8080/api/media/audio/analyze
```

**响应：**
```json
{
  "status": "success",
  "message": "音频分析完成",
  "data": {
    "transcript": "你好，这是一个测试音频",
    "language": "zh-CN",
    "sentiment": "positive",
    "sentimentScore": 0.85,
    "keywords": "测试, 音频",
    "duration": 5,
    "audioQuality": 92,
    "hasNoise": false
  }
}
```

**支持的格式：** mp3, wav, m4a, ogg
**最大文件大小：** 100MB

#### 1.2 语音转文本

**端点：** `POST /api/media/audio/speech-to-text`

**用途：** 提取音频中的文字内容

**请求：**
```bash
curl -X POST \
  -F "file=@speech.wav" \
  http://localhost:8080/api/media/audio/speech-to-text
```

**响应：**
```json
{
  "status": "success",
  "message": "语音转文本完成",
  "transcript": "在这里是识别出来的文本内容"
}
```

#### 1.3 情感分析

**端点：** `POST /api/media/audio/sentiment`

**用途：** 分析音频中表达的情感（正面、负面、中立）

**请求：**
```bash
curl -X POST \
  -F "file=@emotion.mp3" \
  http://localhost:8080/api/media/audio/sentiment
```

**响应：**
```json
{
  "status": "success",
  "message": "情感分析完成",
  "sentiment": "positive"  // positive | negative | neutral
}
```

---

### 二、视频处理 API

#### 2.1 分析视频文件

**端点：** `POST /api/media/video/analyze`

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/analyze
```

**响应：**
```json
{
  "status": "success",
  "message": "视频分析完成",
  "data": {
    "title": "视频标题",
    "description": "这是一个关于...的视频",
    "duration": 120,
    "frameRate": 30,
    "resolution": "1920x1080",
    "objects": ["人", "汽车", "树木"],
    "sceneDescription": "在城市街道上",
    "faceCount": 3,
    "extractedText": "视频中提取的文字",
    "audioTranscript": "音频转录内容",
    "qualityScore": 88,
    "dominantColors": ["蓝色", "白色"],
    "sentiment": "neutral",
    "categories": ["日常生活", "城市"]
  }
}
```

**支持的格式：** mp4, mov, avi, mkv, webm
**最大文件大小：** 500MB

#### 2.2 物体检测

**端点：** `POST /api/media/video/detect-objects`

**用途：** 检测视频中的物体和人物

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/detect-objects
```

**响应：**
```json
{
  "status": "success",
  "message": "物体检测完成",
  "objects": "人, 汽车, 建筑物, 树木"
}
```

#### 2.3 文字提取

**端点：** `POST /api/media/video/extract-text`

**用途：** 从视频中提取所有文字内容（OCR）

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/extract-text
```

**响应：**
```json
{
  "status": "success",
  "message": "文字提取完成",
  "text": "在屏幕上看到的所有文字内容"
}
```

#### 2.4 音频转录

**端点：** `POST /api/media/video/extract-audio`

**用途：** 提取视频中的音频内容并转录为文字

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/extract-audio
```

**响应：**
```json
{
  "status": "success",
  "message": "音频提取完成",
  "transcript": "视频中音频的完整转录"
}
```

#### 2.5 场景描述

**端点：** `POST /api/media/video/scene-description`

**用途：** 获取视频场景的详细描述

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/scene-description
```

**响应：**
```json
{
  "status": "success",
  "message": "场景描述获取完成",
  "description": "这个视频展示了一个繁忙的城市街道，有许多行人和车辆..."
}
```

#### 2.6 人脸检测

**端点：** `POST /api/media/video/detect-faces`

**用途：** 检测视频中的人脸数量

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/detect-faces
```

**响应：**
```json
{
  "status": "success",
  "message": "人脸检测完成",
  "faceCount": 5
}
```

#### 2.7 质量评分

**端点：** `POST /api/media/video/quality-score`

**用途：** 评估视频的质量（分辨率、清晰度等）

**请求：**
```bash
curl -X POST \
  -F "file=@video.mp4" \
  http://localhost:8080/api/media/video/quality-score
```

**响应：**
```json
{
  "status": "success",
  "message": "质量评分获取完成",
  "score": 88  // 0-100
}
```

#### 2.8 获取媒体处理能力

**端点：** `GET /api/media/capabilities`

**用途：** 查看支持的所有格式和功能

**请求：**
```bash
curl http://localhost:8080/api/media/capabilities
```

**响应：**
```json
{
  "status": "success",
  "message": "媒体处理能力",
  "capabilities": {
    "audio": {
      "formats": ["mp3", "wav", "m4a", "ogg"],
      "features": [
        "语音识别 (Speech-to-Text)",
        "情感分析",
        "关键词提取",
        "语言识别"
      ],
      "maxSize": "100MB"
    },
    "video": {
      "formats": ["mp4", "mov", "avi", "mkv", "webm"],
      "features": [
        "视频理解",
        "物体检测",
        "人脸检测",
        "文字提取",
        "场景识别",
        "音频转录",
        "质量评分"
      ],
      "maxSize": "500MB"
    }
  }
}
```

---

## 🔧 高级用法

### 使用 Python 调用 API

```python
import requests

# 分析音频
def analyze_audio(file_path):
    with open(file_path, 'rb') as f:
        files = {'file': f}
        response = requests.post(
            'http://localhost:8080/api/media/audio/analyze',
            files=files
        )
    return response.json()

# 分析视频
def analyze_video(file_path):
    with open(file_path, 'rb') as f:
        files = {'file': f}
        response = requests.post(
            'http://localhost:8080/api/media/video/analyze',
            files=files
        )
    return response.json()

# 检测视频中的物体
def detect_objects(file_path):
    with open(file_path, 'rb') as f:
        files = {'file': f}
        response = requests.post(
            'http://localhost:8080/api/media/video/detect-objects',
            files=files
        )
    return response.json()

# 使用示例
result = analyze_audio('speech.wav')
print(result['data']['transcript'])
```

### 使用 JavaScript/Node.js 调用 API

```javascript
const FormData = require('form-data');
const fs = require('fs');
const axios = require('axios');

async function analyzeAudio(filePath) {
    const formData = new FormData();
    formData.append('file', fs.createReadStream(filePath));

    try {
        const response = await axios.post(
            'http://localhost:8080/api/media/audio/analyze',
            formData,
            { headers: formData.getHeaders() }
        );
        return response.data;
    } catch (error) {
        console.error('Error:', error.message);
    }
}

async function analyzeVideo(filePath) {
    const formData = new FormData();
    formData.append('file', fs.createReadStream(filePath));

    try {
        const response = await axios.post(
            'http://localhost:8080/api/media/video/analyze',
            formData,
            { headers: formData.getHeaders() }
        );
        return response.data;
    } catch (error) {
        console.error('Error:', error.message);
    }
}

// 使用示例
(async () => {
    const result = await analyzeAudio('speech.wav');
    console.log(result.data.transcript);
})();
```

---

## 📚 架构设计

### 服务层架构

```
┌─────────────────────────────┐
│   MediaController           │ ← REST API 层
├─────────────────────────────┤
│   AudioService              │ ← 业务逻辑层
│   VideoService              │
├─────────────────────────────┤
│   AudioAnalysisResult       │ ← 数据模型层
│   VideoAnalysisResult       │
├─────────────────────────────┤
│   OkHttpClient              │ ← HTTP 客户端
├─────────────────────────────┤
│   智谱 AI API               │ ← 大模型服务
└─────────────────────────────┘
```

### 流程图

**音频处理流程：**
```
用户上传音频
    ↓
验证文件格式和大小
    ↓
转换为 Base64
    ↓
调用智谱音频 API
    ↓
解析响应
    ↓
返回 AudioAnalysisResult
```

**视频处理流程：**
```
用户上传视频
    ↓
验证文件格式和大小
    ↓
转换为 Base64
    ↓
调用智谱视频 API
    ↓
解析响应
    ↓
返回 VideoAnalysisResult
```

---

## 🎓 核心概念解析

### 1. 音频识别（ASR）

**什么是音频识别？**

音频识别（Automatic Speech Recognition, ASR）是将人类说话的音频信号转换为文字的过程。

```
音频波形 → 特征提取 → 深度学习模型 → 文字输出
   🔊      →    波谱   →   神经网络   →  "你好"
```

**你的项目实现：**
- 支持多种格式（mp3, wav, m4a, ogg）
- 自动语言检测
- 实时转录

### 2. 视频理解

**什么是视频理解？**

视频理解是指计算机对视频内容进行分析，识别其中的对象、场景、动作等。

```
视频帧 → 提取特征 → 多模态模型 → 语义理解
  📹  →  图像特征  →  视觉 + 语言  →  "街道上有3个人"
```

**你的项目支持的能力：**
- 物体检测（Object Detection）
- 人脸识别（Face Recognition）
- 文字识别（OCR）
- 场景理解（Scene Understanding）
- 音频转录（Audio Transcription）

### 3. 多模态处理

**什么是多模态？**

处理多种类型的数据（文本、图像、音频、视频）的 AI 模型。

```
├─ 文本 → 语言模型
├─ 图像 → 视觉模型  → 统一的语义空间 → 综合理解
├─ 音频 → 音频模型
└─ 视频 → 视频模型（文本+图像+音频）
```

---

## 🚨 错误处理

### 常见错误和解决方案

| 错误 | 原因 | 解决方案 |
|-----|------|--------|
| 403 Unauthorized | API Key 无效 | 检查 `ZHIPU_API_KEY` 环境变量 |
| 400 Bad Request | 文件格式不支持 | 确保使用支持的格式（mp3, mp4 等） |
| 413 Payload Too Large | 文件超过大小限制 | 音频 < 100MB，视频 < 500MB |
| 504 Gateway Timeout | API 响应超时 | 增加 `media-timeout` 值 |

---

## 📈 性能优化

### 1. 并发处理

使用线程池处理多个请求：

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> audioService.analyzeAudio(file));
```

### 2. 缓存策略

缓存分析结果避免重复处理：

```java
@Cacheable(value = "audioAnalysis", key = "#file.name")
public AudioAnalysisResult analyzeAudio(MultipartFile file) {
    // ...
}
```

### 3. 异步处理

使用异步 API 处理大文件：

```java
@Async
public CompletableFuture<VideoAnalysisResult> analyzeVideoAsync(MultipartFile file) {
    // ...
}
```

---

## 🔐 安全建议

1. **API Key 管理**
   - 使用环境变量存储 API Key
   - 定期轮换 API Key
   - 不要在代码中硬编码 API Key

2. **文件上传验证**
   - 验证文件类型和大小
   - 使用病毒扫描
   - 限制上传频率

3. **访问控制**
   - 启用身份认证（JWT）
   - 设置速率限制
   - 记录所有操作

---

## 📝 日志示例

```
2026-03-02 12:00:00 INFO 接收音频分析请求: speech.wav
2026-03-02 12:00:01 INFO 开始分析音频文件: speech.wav
2026-03-02 12:00:03 INFO 音频分析完成: 文本=你好，请问有什么帮助吗？, 时长=3
2026-03-02 12:00:03 INFO 接收视频分析请求: video.mp4
2026-03-02 12:00:05 INFO 开始分析视频文件: video.mp4
2026-03-02 12:00:30 INFO 视频分析完成: 标题=城市街道
```

---

## 🤝 扩展方向

1. **集成更多模型**
   - 实时字幕生成
   - 视频摘要生成
   - 情绪检测增强

2. **处理优化**
   - 增量处理（逐帧处理大视频）
   - GPU 加速
   - 分布式处理

3. **应用集成**
   - 实时转录应用
   - 视频内容管理系统
   - 可访问性增强工具

---

## 📞 支持

如有问题或建议，请提交 Issue 或联系开发团队。

祝你使用愉快！🎉

