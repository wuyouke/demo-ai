#!/bin/bash

# 媒体 API 测试脚本
# 用法: bash test_media_api.sh

API_BASE_URL="http://localhost:8080/api/media"

echo "========================================"
echo "智谱音视频模型集成 API 测试"
echo "========================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 获取媒体处理能力
echo -e "${BLUE}[1] 获取媒体处理能力${NC}"
curl -s "${API_BASE_URL}/capabilities" | jq '.'
echo ""

# 2. 创建测试音频文件
echo -e "${BLUE}[2] 创建测试音频文件${NC}"
if ! command -v ffmpeg &> /dev/null; then
    echo -e "${YELLOW}⚠️  ffmpeg 未安装，跳过音频测试${NC}"
    echo "请使用以下命令安装: brew install ffmpeg"
else
    # 创建一个简单的测试音频
    ffmpeg -f lavfi -i "sine=frequency=1000:duration=3" -q:a 9 -acodec libmp3lame test_audio.mp3 -y 2>/dev/null
    echo "✅ 测试音频创建完成: test_audio.mp3"
    echo ""

    # 3. 测试音频分析
    echo -e "${BLUE}[3] 分析音频文件${NC}"
    curl -s -X POST \
        -F "file=@test_audio.mp3" \
        "${API_BASE_URL}/audio/analyze" | jq '.'
    echo ""

    # 4. 测试语音转文本
    echo -e "${BLUE}[4] 语音转文本${NC}"
    curl -s -X POST \
        -F "file=@test_audio.mp3" \
        "${API_BASE_URL}/audio/speech-to-text" | jq '.'
    echo ""

    # 5. 测试情感分析
    echo -e "${BLUE}[5] 情感分析${NC}"
    curl -s -X POST \
        -F "file=@test_audio.mp3" \
        "${API_BASE_URL}/audio/sentiment" | jq '.'
    echo ""
fi

# 6. 创建测试视频文件
echo -e "${BLUE}[6] 创建测试视频文件${NC}"
if ! command -v ffmpeg &> /dev/null; then
    echo -e "${YELLOW}⚠️  ffmpeg 未安装，跳过视频测试${NC}"
else
    # 创建一个简单的测试视频
    ffmpeg -f lavfi -i "color=c=blue:s=320x240:d=5" -pix_fmt yuv420p test_video.mp4 -y 2>/dev/null
    echo "✅ 测试视频创建完成: test_video.mp4"
    echo ""

    # 7. 测试视频分析
    echo -e "${BLUE}[7] 分析视频文件${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/analyze" | jq '.'
    echo ""

    # 8. 物体检测
    echo -e "${BLUE}[8] 物体检测${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/detect-objects" | jq '.'
    echo ""

    # 9. 文字提取
    echo -e "${BLUE}[9] 文字提取${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/extract-text" | jq '.'
    echo ""

    # 10. 音频转录
    echo -e "${BLUE}[10] 音频转录${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/extract-audio" | jq '.'
    echo ""

    # 11. 场景描述
    echo -e "${BLUE}[11] 场景描述${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/scene-description" | jq '.'
    echo ""

    # 12. 人脸检测
    echo -e "${BLUE}[12] 人脸检测${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/detect-faces" | jq '.'
    echo ""

    # 13. 质量评分
    echo -e "${BLUE}[13] 质量评分${NC}"
    curl -s -X POST \
        -F "file=@test_video.mp4" \
        "${API_BASE_URL}/video/quality-score" | jq '.'
    echo ""
fi

echo -e "${GREEN}========================================"
echo "✅ 所有测试完成！"
echo "========================================${NC}"

# 清理测试文件
echo -e "${YELLOW}清理测试文件...${NC}"
rm -f test_audio.mp3 test_video.mp4
echo "✅ 清理完成"

