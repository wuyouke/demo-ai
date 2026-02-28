import urllib.request
import urllib.error
import time

url = "http://localhost:8080/api/chat/health"

print("开始测试限流 (每秒最多 2 次请求)...")
for i in range(5):
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req) as response:
            print(f"请求 {i+1}: 成功! 状态码: {response.status}")
    except urllib.error.HTTPError as e:
        print(f"请求 {i+1}: 被拦截! 状态码: {e.code} - 响应: {e.read().decode('utf-8')}")
    except Exception as e:
        print(f"请求 {i+1}: 发生错误: {e}")

