#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
智谱音视频模型集成 API 测试脚本
用法: python test_media_api.py
"""

import requests
import json
import sys
from pathlib import Path

# 配置
API_BASE_URL = "http://localhost:8080/api/media"
TIMEOUT = 60

class Colors:
    """ANSI 颜色代码"""
    GREEN = '\033[92m'
    BLUE = '\033[94m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    END = '\033[0m'

def print_header(title):
    """打印标题"""
    print(f"{Colors.BLUE}{'='*50}{Colors.END}")
    print(f"{Colors.BLUE}{title}{Colors.END}")
    print(f"{Colors.BLUE}{'='*50}{Colors.END}\n")

def print_success(message):
    """打印成功信息"""
    print(f"{Colors.GREEN}✓ {message}{Colors.END}")

def print_error(message):
    """打印错误信息"""
    print(f"{Colors.RED}✗ {message}{Colors.END}")

def print_info(message):
    """打印信息"""
    print(f"{Colors.BLUE}ℹ {message}{Colors.END}")

def print_json(data):
    """美化打印 JSON"""
    print(json.dumps(data, indent=2, ensure_ascii=False))

def check_server():
    """检查服务器是否运行"""
    try:
        response = requests.get(f"{API_BASE_URL}/capabilities", timeout=5)
        if response.status_code == 200:
            print_success("服务器正在运行")
            return True
    except requests.exceptions.ConnectionError:
        print_error("无法连接到服务器，请确保应用在 http://localhost:8080 上运行")
        return False

def get_capabilities():
    """获取媒体处理能力"""
    print_header("1. 获取媒体处理能力")

    try:
        response = requests.get(f"{API_BASE_URL}/capabilities", timeout=TIMEOUT)
        if response.status_code == 200:
            print_success("获取能力信息成功")
            data = response.json()

            # 显示音频能力
            audio_caps = data.get('capabilities', {}).get('audio', {})
            print(f"\n{Colors.BLUE}音频处理能力：{Colors.END}")
            print(f"  格式: {', '.join(audio_caps.get('formats', []))}")
            print(f"  功能: {', '.join(audio_caps.get('features', []))}")
            print(f"  最大大小: {audio_caps.get('maxSize', 'N/A')}")

            # 显示视频能力
            video_caps = data.get('capabilities', {}).get('video', {})
            print(f"\n{Colors.BLUE}视频处理能力：{Colors.END}")
            print(f"  格式: {', '.join(video_caps.get('formats', []))}")
            print(f"  功能: {', '.join(video_caps.get('features', []))}")
            print(f"  最大大小: {video_caps.get('maxSize', 'N/A')}")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"获取能力失败: {str(e)}")

def test_audio_analysis(file_path):
    """测试音频分析"""
    print_header("2. 分析音频文件")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return None

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/audio/analyze",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("音频分析成功")
            data = response.json()

            result = data.get('data', {})
            print(f"\n分析结果:")
            print(f"  文本: {result.get('transcript', 'N/A')}")
            print(f"  语言: {result.get('language', 'N/A')}")
            print(f"  情感: {result.get('sentiment', 'N/A')}")
            print(f"  情感得分: {result.get('sentimentScore', 'N/A')}")
            print(f"  时长: {result.get('duration', 'N/A')} 秒")
            print(f"  音质: {result.get('audioQuality', 'N/A')}/100")
            print(f"  包含噪音: {result.get('hasNoise', 'N/A')}")

            return result
        else:
            print_error(f"请求失败: {response.status_code}")
            print_error(f"响应: {response.text}")
    except Exception as e:
        print_error(f"音频分析失败: {str(e)}")

    return None

def test_speech_to_text(file_path):
    """测试语音转文本"""
    print_header("3. 语音转文本")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/audio/speech-to-text",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("语音转文本成功")
            data = response.json()
            print(f"\n转录结果: {data.get('transcript', 'N/A')}")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"语音转文本失败: {str(e)}")

def test_sentiment_analysis(file_path):
    """测试情感分析"""
    print_header("4. 情感分析")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/audio/sentiment",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("情感分析成功")
            data = response.json()
            print(f"\n情感: {data.get('sentiment', 'N/A')}")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"情感分析失败: {str(e)}")

def test_video_analysis(file_path):
    """测试视频分析"""
    print_header("5. 分析视频文件")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return None

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/video/analyze",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("视频分析成功")
            data = response.json()

            result = data.get('data', {})
            print(f"\n分析结果:")
            print(f"  标题: {result.get('title', 'N/A')}")
            print(f"  描述: {result.get('description', 'N/A')}")
            print(f"  时长: {result.get('duration', 'N/A')} 秒")
            print(f"  分辨率: {result.get('resolution', 'N/A')}")
            print(f"  帧率: {result.get('frameRate', 'N/A')} fps")
            print(f"  物体: {', '.join(result.get('objects', []))}")
            print(f"  人脸数: {result.get('faceCount', 'N/A')}")
            print(f"  质量分: {result.get('qualityScore', 'N/A')}/100")
            print(f"  情感: {result.get('sentiment', 'N/A')}")

            return result
        else:
            print_error(f"请求失败: {response.status_code}")
            print_error(f"响应: {response.text}")
    except Exception as e:
        print_error(f"视频分析失败: {str(e)}")

    return None

def test_object_detection(file_path):
    """测试物体检测"""
    print_header("6. 物体检测")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/video/detect-objects",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("物体检测成功")
            data = response.json()
            print(f"\n检测结果: {data.get('objects', 'N/A')}")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"物体检测失败: {str(e)}")

def test_text_extraction(file_path):
    """测试文字提取"""
    print_header("7. 文字提取")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/video/extract-text",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("文字提取成功")
            data = response.json()
            print(f"\n提取结果: {data.get('text', 'N/A')}")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"文字提取失败: {str(e)}")

def test_face_detection(file_path):
    """测试人脸检测"""
    print_header("8. 人脸检测")

    if not Path(file_path).exists():
        print_error(f"文件不存在: {file_path}")
        return

    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                f"{API_BASE_URL}/video/detect-faces",
                files=files,
                timeout=TIMEOUT
            )

        if response.status_code == 200:
            print_success("人脸检测成功")
            data = response.json()
            print(f"\n检测结果: 发现 {data.get('faceCount', 'N/A')} 张脸")
        else:
            print_error(f"请求失败: {response.status_code}")
    except Exception as e:
        print_error(f"人脸检测失败: {str(e)}")

def main():
    """主函数"""
    print(f"\n{Colors.BLUE}")
    print("╔═══════════════════════════════════════╗")
    print("║  智谱音视频模型集成 API 测试脚本      ║")
    print("╚═══════════════════════════════════════╝")
    print(f"{Colors.END}\n")

    # 检查服务器
    if not check_server():
        sys.exit(1)

    print()

    # 获取能力信息
    get_capabilities()

    # 提示用户
    print(f"\n{Colors.YELLOW}提示:{Colors.END}")
    print("1. 请将你的音频文件放在当前目录，并将下面的 'audio.mp3' 替换为实际文件名")
    print("2. 请将你的视频文件放在当前目录，并将下面的 'video.mp4' 替换为实际文件名")
    print("3. 如果文件不存在，某些测试将被跳过\n")

    # 测试音频
    audio_file = "audio.mp3"
    if Path(audio_file).exists():
        test_audio_analysis(audio_file)
        test_speech_to_text(audio_file)
        test_sentiment_analysis(audio_file)
    else:
        print_info(f"跳过音频测试 ({audio_file} 不存在)")

    print()

    # 测试视频
    video_file = "video.mp4"
    if Path(video_file).exists():
        test_video_analysis(video_file)
        test_object_detection(video_file)
        test_text_extraction(video_file)
        test_face_detection(video_file)
    else:
        print_info(f"跳过视频测试 ({video_file} 不存在)")

    print()
    print(f"{Colors.GREEN}{'='*50}")
    print("✅ 所有测试完成！")
    print(f"{'='*50}{Colors.END}\n")

if __name__ == "__main__":
    main()

