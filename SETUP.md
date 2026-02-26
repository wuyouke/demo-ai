# 环境配置说明

## 🔐 API Key 配置

为了保护你的 API Key 安全，本工程使用环境变量来存储敏感信息。

### 方法 1：使用 .env 文件（推荐）

1. 复制示例配置文件：
```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，填入你的 API Key：
```bash
# 智谱 AI API Key
ZHIPU_API_KEY=你的实际API密钥

# OpenAI API Key（可选）
OPENAI_API_KEY=你的OpenAI密钥
```

3. 运行应用：
```bash
source .env && mvn spring-boot:run
```

### 方法 2：使用环境变量

```bash
# Mac/Linux
export ZHIPU_API_KEY=你的实际API密钥
export OPENAI_API_KEY=你的OpenAI密钥（可选）
mvn spring-boot:run

# Windows PowerShell
$env:ZHIPU_API_KEY="你的实际API密钥"
$env:OPENAI_API_KEY="你的OpenAI密钥"
mvn spring-boot:run
```

### 方法 3：在 IDE 中配置

#### IntelliJ IDEA
1. Run → Edit Configurations
2. 选择你的 Spring Boot 运行配置
3. 在 Environment variables 中添加：
   - `ZHIPU_API_KEY=你的实际API密钥`
   - `OPENAI_API_KEY=你的OpenAI密钥`（可选）

#### VS Code
在 `launch.json` 中添加环境变量，或在终端中设置后运行。

## 📋 获取 API Key

### 智谱 AI
1. 访问：https://open.bigmodel.cn/
2. 注册/登录账号
3. 进入 API 管理页面创建 API Key
4. 免费额度充足，适合测试使用

### OpenAI
1. 访问：https://platform.openai.com/api-keys
2. 登录账号
3. 创建新的 API Key

## ⚠️ 安全注意事项

- ✅ **不要**将 `.env` 文件提交到 git
- ✅ **不要**在代码中硬编码 API Key
- ✅ **不要**在公开平台分享 API Key
- ✅ 定期更换 API Key
- ✅ 使用不同的 API Key 用于不同环境（开发、测试、生产）

## 🚀 验证配置

启动应用后，访问健康检查接口验证：
```bash
curl http://localhost:8080/api/chat/health
```

如果看到 "AI Chat Service is running!" 则说明配置成功。

