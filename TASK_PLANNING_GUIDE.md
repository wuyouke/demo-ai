# 🤖 智能任务规划系统使用指南

## 概述

智能任务规划系统是一个强大的任务分解和执行框架，能够将复杂的用户目标自动分解成多个可执行的子任务，并支持依赖关系管理、并行执行和监控。

## 系统架构

### 核心组件

1. **Task（任务模型）** - `Task.java`
   - 表示一个可执行的任务
   - 包含任务类型、状态、优先级、依赖关系等信息
   - 支持时间追踪和结果存储

2. **TaskPlanner（任务规划器）** - `TaskPlanner.java`
   - 使用 AI（智谱 GLM-4 模型）分解复杂目标
   - 自动检测任务依赖关系
   - 进行拓扑排序以确保执行顺序正确
   - 支持降级方案（当 AI 规划失败时）

3. **TaskExecutor（任务执行器）** - `TaskExecutor.java`
   - 按照依赖关系执行任务
   - 支持并行执行（限制并发数）
   - 支持顺序执行（严格按依赖）
   - 提供执行监听器接口用于监控
   - 收集执行统计信息

4. **TaskPlanningController（REST API）** - `TaskPlanningController.java`
   - 暴露任务规划 REST API
   - 支持任务规划、执行、验证和顺序查询

## 使用流程

### 1. 前端页面访问

访问 `http://localhost:8888/task-planning.html`

### 2. 任务规划

**API 端点**：`POST /api/tasks/plan`

**请求示例**：
```json
{
  "goal": "帮我查询明天的天气，并推荐该地区的旅游景点",
  "context": {
    "location": "北京",
    "user_preference": "自然景观"
  }
}
```

**响应示例**：
```json
{
  "success": true,
  "goal": "帮我查询明天的天气，并推荐该地区的旅游景点",
  "tasks": [
    {
      "taskId": "xxx-xxx-xxx",
      "name": "查询天气",
      "description": "获取北京地区明天的天气信息",
      "type": "INFORMATION_QUERY",
      "toolName": "weather",
      "toolParams": { "location": "北京" },
      "priority": 10,
      "status": "PENDING",
      "dependencies": []
    },
    {
      "taskId": "yyy-yyy-yyy",
      "name": "推荐旅游景点",
      "description": "根据天气和用户偏好推荐旅游景点",
      "type": "INFORMATION_QUERY",
      "toolName": "tourist_attraction",
      "toolParams": { "location": "北京", "type": "natural" },
      "priority": 8,
      "status": "PENDING",
      "dependencies": ["xxx-xxx-xxx"]
    }
  ],
  "taskCount": 2,
  "plan": "📋 **任务规划结果**..."
}
```

### 3. 任务执行

**API 端点**：`POST /api/tasks/execute`

**请求示例**：
```json
{
  "tasks": [...],
  "sequential": false
}
```

**参数说明**：
- `tasks`: 任务列表（来自规划结果）
- `sequential`:
  - `true` - 严格顺序执行（按依赖关系）
  - `false` - 并行执行（受并发限制，默认3个并发）

**响应示例**：
```json
{
  "success": true,
  "tasks": [
    {
      "taskId": "xxx-xxx-xxx",
      "name": "查询天气",
      "status": "COMPLETED",
      "result": { "temp": 15, "condition": "晴天" },
      "executionTime": 1234
    },
    {
      "taskId": "yyy-yyy-yyy",
      "name": "推荐旅游景点",
      "status": "COMPLETED",
      "result": { "attractions": [...] },
      "executionTime": 2345
    }
  ],
  "stats": {
    "totalTasks": 2,
    "completedTasks": 2,
    "failedTasks": 0,
    "successRate": "100.0%",
    "totalExecutionTime": 3579
  }
}
```

### 4. 获取执行顺序

**API 端点**：`POST /api/tasks/order`

用于查看任务的推荐执行顺序（基于优先级和依赖关系）。

### 5. 验证依赖关系

**API 端点**：`POST /api/tasks/validate`

检测任务列表中是否存在循环依赖。

## 任务类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `TOOL_CALL` | 工具调用 | 调用天气工具、推荐景点工具等 |
| `INFORMATION_QUERY` | 信息查询 | 查询天气、搜索信息 |
| `DATA_ANALYSIS` | 数据分析 | 分析数据、生成报表 |
| `DECISION_MAKING` | 决策制定 | 基于分析结果做出决策 |
| `PLANNING` | 计划制定 | 制定详细计划 |
| `COMMUNICATION` | 通讯 | 发送通知、消息 |
| `OTHER` | 其他 | 其他类型 |

## 任务状态

| 状态 | 说明 |
|------|------|
| `PENDING` | 待执行 |
| `EXECUTING` | 执行中 |
| `COMPLETED` | 已完成 |
| `FAILED` | 失败 |
| `SKIPPED` | 已跳过 |

## 前端界面特性

### 左侧：任务规划面板
- 输入目标和上下文
- 查看规划结果
- 一键执行任务

### 右侧：执行监控面板
- **任务列表**：实时查看任务执行状态
- **执行统计**：查看执行统计信息
  - 总任务数
  - 已完成任务
  - 失败任务
  - 成功率
  - 执行进度条

### 功能按钮
- **规划任务**：触发 AI 任务分解
- **执行任务**：开始执行任务
- **清空任务**：清空当前任务列表
- **导出结果**：将结果导出为 JSON 文件
- **验证依赖**：检查是否存在循环依赖

## 技术细节

### 依赖检测算法

TaskPlanner 使用以下启发式规则自动检测任务依赖：

1. **查询 → 分析依赖**
   - 如果任务是 `INFORMATION_QUERY`，后续的 `DATA_ANALYSIS` 任务会自动依赖它

2. **规划 → 执行依赖**
   - 如果任务是 `PLANNING`，后续的 `TOOL_CALL` 任务会自动依赖它

3. **拓扑排序**
   - 使用深度优先搜索（DFS）进行拓扑排序
   - 检测循环依赖

### 并发执行策略

- 默认最多 3 个任务并发执行
- 可通过 `TaskExecutor` 构造函数参数调整
- 自动等待依赖任务完成再执行

### 错误处理

- 任务失败时记录错误信息
- 不影响其他任务执行
- 支持重试（通过重新执行）

## 集成示例

### Java 代码集成

```java
// 1. 注入依赖
@Autowired
private ConversationService conversationService;

@Autowired
private ToolRegistry toolRegistry;

// 2. 创建规划器
TaskPlanner planner = new TaskPlanner(conversationService);

// 3. 规划任务
List<Task> tasks = planner.planTasks(
    "查询天气并推荐景点",
    Collections.singletonMap("location", "北京")
);

// 4. 创建执行器
TaskExecutor executor = new TaskExecutor(toolRegistry);

// 5. 添加监听器（可选）
executor.addListener(new TaskExecutor.TaskExecutionListener() {
    @Override
    public void onTaskStarted(Task task) {
        logger.info("任务开始: {}", task.getName());
    }

    @Override
    public void onTaskCompleted(Task task) {
        logger.info("任务完成: {}", task.getName());
    }

    @Override
    public void onTaskFailed(Task task, Exception e) {
        logger.error("任务失败: {}", task.getName(), e);
    }
});

// 6. 执行任务（并行）
List<Task> executedTasks = executor.executeTasks(tasks);

// 7. 获取统计
ExecutionStats stats = executor.getExecutionStats(executedTasks);
System.out.println(stats); // 输出统计信息

// 8. 关闭执行器
executor.shutdown();
```

## 常见问题

### Q: 如何自定义任务执行逻辑？
A: 通过 `ToolRegistry` 注册自定义工具执行器，实现 `ToolExecutor` 接口即可。

### Q: 如何调整并发执行数？
A: 创建 TaskExecutor 时指定并发数：
```java
TaskExecutor executor = new TaskExecutor(toolRegistry, 5); // 5个并发
```

### Q: 任务执行失败时会怎样？
A:
- 任务状态变为 `FAILED`
- 其他不依赖它的任务继续执行
- 依赖它的任务会被跳过或失败

### Q: 如何修改 AI 规划的行为？
A: 在 `TaskPlanner.buildPlanningPrompt()` 中修改提示词，或调整 `parseTaskPlanning` 中的解析逻辑。

### Q: 支持哪些工具？
A: 系统支持任何实现了 `ToolExecutor` 接口的工具：
- 天气工具 (weather)
- 旅游景点工具 (tourist_attraction)
- 视频分析工具 (video_analysis)
- 音频分析工具 (audio_analysis)
- 自定义工具

## 最佳实践

1. **合理设置优先级**
   - 重要的任务设置高优先级（8-10）
   - 常规任务设置中等优先级（5-7）
   - 可选任务设置低优先级（1-3）

2. **利用依赖关系**
   - 显式声明任务依赖关系
   - 避免循环依赖
   - 允许系统自动检测隐式依赖

3. **错误处理**
   - 监听 `onTaskFailed` 事件
   - 实现重试逻辑
   - 记录详细的错误信息

4. **性能优化**
   - 使用并行执行提高效率
   - 合理设置并发数
   - 避免过度分解任务

5. **监控和日志**
   - 使用执行监听器跟踪进度
   - 定期检查执行统计
   - 保存执行结果用于分析

## 下一步扩展

- [ ] 任务调度（定时执行）
- [ ] 任务持久化（数据库存储）
- [ ] 任务优先级动态调整
- [ ] 更复杂的依赖关系表达式
- [ ] 任务失败重试机制
- [ ] 分布式任务执行
- [ ] 任务执行历史和分析

---

**更新时间**: 2026-03-25
**版本**: 1.0.0

