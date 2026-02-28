# 在 IntelliJ IDEA 中启动应用指南

## 重要提示：不要在代码中硬编码 API Key！

为了安全起见，API Key 已经从 `application.yml` 中移除，请使用以下方法之一配置：

---

## 方法 1：在 IDEA 的 Run Configuration 中配置环境变量（推荐）

1. 在 IDEA 中找到 `DemoAiApplication.java` 类
2. 右键点击，选择 "Run 'DemoAiApplication.main()'"
3. 第一次运行后，在右上角的下拉菜单中点击 "Edit Configurations..."
4. 找到 `DemoAiApplication` 配置
5. 在 "Environment variables" 字段中添加：
   ```
   ZHIPU_API_KEY=你的API-Key
   ```
6. 点击 "Apply" 和 "OK"
7. 再次点击 "Run" 按钮（绿色三角形）

---

## 方法 2：使用 Maven 插件启动

### 步骤 1：设置环境变量
```bash
export ZHIPU_API_KEY=你的API-Key
```

### 步骤 2：使用 Maven 启动
1. 打开 IDEA 右侧的 "Maven" 工具窗口
2. 展开 `demo-ai` -> `Plugins` -> `spring-boot`
3. 双击 `spring-boot:run`

---

## 方法 3：在 IDEA 中设置全局环境变量

1. 打开 IDEA 的 "Preferences" (Mac) 或 "Settings" (Windows/Linux)
2. 导航到 "Build, Execution, Deployment" -> "Build Tools" -> "Maven" -> "Runner"
3. 在 "Environment variables" 中添加：
   ```
   ZHIPU_API_KEY=你的API-Key
   ```
4. 点击 "Apply"

---

## API Key 来源

如果你的 `.env` 文件中已经有 API Key，可以这样导入：

```bash
# 在 IDEA 的 Terminal 中执行
export ZHIPU_API_KEY=$(grep "^ZHIPU_API_KEY=" .env | cut -d '=' -f2-)
```

然后使用 Maven 插件启动。

---

## 验证配置是否成功

启动应用后，如果看到以下日志说明配置成功：

```
INFO c.e.demo_ai.config.LangChain4jConfig : 智谱 AI API Key 已配置
INFO c.e.demo_ai.config.LangChain4jConfig : 使用智谱 AI 模型: glm-4-flash
```

如果看到错误提示 "智谱 AI API Key 未配置"，请检查环境变量是否正确设置。

---

## 安全提示

⚠️ **重要**：
- 不要将 API Key 提交到 Git 仓库
- `.env` 文件已在 `.gitignore` 中
- `application.yml` 不包含真实的 API Key
- 生产环境应使用更安全的方式管理密钥（如 Vault、K8s Secrets 等）

