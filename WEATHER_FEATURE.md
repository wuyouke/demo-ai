# Function Tools 天气查询功能说明

## 功能概述

本项目成功实现了基于 LangChain4j 的 **Function Calling / Function Tools** 功能，让 AI 模型能够自动调用外部工具来查询实时天气信息。

## 核心特性

### 1. Function Tools
- 使用 LangChain4j 的 `@Tool` 注解定义 AI 可调用的函数
- 自动函数调用：AI 智能判断何时需要调用天气查询工具
- 多粒度工具：提供完整的天气信息、温度、天气状况等不同工具

### 2. 模拟天气服务
- 使用 Mock 数据模拟天气 API（避免真实 API Key 限制）
- 支持多个城市（北京、上海、广州、深圳、成都、杭州等）
- 自动生成随机数据（对于未配置的城市）

## API 接口

### 1. 健康检查
```bash
GET /api/weather/health
```
响应：
```json
{
  "status": "OK",
  "message": "天气服务运行正常",
  "service": "Weather Service with Function Calling"
}
```

### 2. 直接查询天气
```bash
POST /api/weather/query
Content-Type: application/json

{
  "city": "北京",
  "province": "北京",
  "country": "中国"
}
```

响应：
```json
{
  "success": true,
  "location": "北京",
  "temperature": 15.5,
  "weather": "晴",
  "humidity": 45,
  "windSpeed": 12.0,
  "windDirection": "西北风",
  "pressure": 1013,
  "visibility": 10.0,
  "aqi": 85,
  "updateTime": "2026-02-27 15:39:42"
}
```

### 3. 智能对话（支持 Function Calling）
```bash
POST /api/weather/chat
Content-Type: application/json

{
  "message": "北京今天天气怎么样"
}
```

AI 会自动判断需要调用天气工具，并返回：
```json
{
  "response": "根据您的查询，我已经调用了天气API，并得到了北京今天的天气状况。据API返回结果显示，北京今天的天气状况是晴。希望这个信息对您有所帮助。",
  "sessionId": "18e636f4-ba5a-4b68-a17e-7def38784b45",
  "success": true
}
```

## 支持的查询示例

### 智能对话示例
- "北京今天天气怎么样" → AI 调用天气工具
- "上海现在多少度" → AI 调用温度查询工具
- "广州现在天气怎么样" → AI 调用天气状况工具
- "告诉我深圳的天气情况" → AI 调用完整天气工具

### 直接查询示例
- 查询北京天气：`{ "city": "北京" }`
- 查询上海温度：`{ "city": "上海" }`
- 查询广州天气：`{ "city": "广州" }`

## 技术实现

### 1. 工具类（WeatherTools）
使用 `@Tool` 注解定义 AI 可调用的函数：

```java
@Tool("查询指定城市的实时天气信息，包括温度、天气状况、湿度、风速等数据")
public String getCurrentWeather(String city) {
    // 实现逻辑
}

@Tool("查询指定城市的当前温度")
public String getTemperature(String city) {
    // 实现逻辑
}

@Tool("查询指定城市的天气状况，如晴、多云、雨等")
public String getWeatherCondition(String city) {
    // 实现逻辑
}
```

### 2. 服务集成
通过 `AiServices` 将工具集成到 AI Assistant：

```java
this.assistant = AiServices.builder(AssistantWithTools.class)
    .chatLanguageModel(chatLanguageModel)
    .tools(weatherTools)
    .build();
```

### 3. Mock 数据
为演示目的，使用模拟天气数据：
- 预设多个城市的天气数据
- 对于未知城市，自动生成随机数据
- 无需真实 API Key，方便测试和学习

## 项目结构

```
src/main/java/com/example/demo_ai/
├── tools/
│   └── WeatherTools.java              # Function Tools 定义
├── service/
│   ├── MockWeatherService.java        # 模拟天气服务
│   ├── WeatherService.java            # 真实天气服务（可选）
│   └── ChatServiceWithTools.java      # 集成 Tools 的对话服务
├── controller/
│   └── WeatherController.java         # 天气 API 控制器
└── model/
    ├── WeatherRequest.java            # 天气请求模型
    └── WeatherResponse.java           # 天气响应模型
```

## 使用场景

1. **智能客服**：用户询问天气，AI 自动调用工具获取实时信息
2. **旅行助手**：提供目的地天气查询和建议
3. **日程安排**：根据天气情况推荐活动
4. **农业咨询**：提供天气数据支持农业决策

## 扩展建议

### 1. 集成真实天气 API
- 注册心知天气（https://www.seniverse.com/）
- 或使用 OpenWeatherMap（https://openweathermap.org/）
- 修改 `WeatherService` 使用真实 API

### 2. 添加更多工具
- 天气预报（未来几天）
- 空气质量指数（AQI）
- 紫外线指数
- 生活建议（穿衣、运动等）

### 3. 支持更多功能
- 地理位置自动识别
- 多语言支持
- 图表展示
- 历史天气查询

## Function Tools vs MCP

### Function Tools（本项目采用）
✅ LangChain4j 原生支持
✅ 实现简单，学习成本低
✅ 适合单机应用
✅ 调试方便

### MCP (Model Context Protocol)
✅ 跨应用标准化
✅ 插件生态丰富
✅ 适合企业级应用
❌ 学习曲线陡峭
❌ 架构复杂

## 总结

本项目成功展示了如何使用 Function Tools 实现 AI 与外部服务的集成：
- ✅ Function Calling 完整实现
- ✅ 智能工具调用
- ✅ 模拟数据服务
- ✅ RESTful API 接口
- ✅ 易于扩展和维护

这是一个很好的起点，可以基于此扩展更多的工具和功能！

