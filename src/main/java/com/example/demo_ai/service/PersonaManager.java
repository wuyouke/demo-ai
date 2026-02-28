package com.example.demo_ai.service;

import com.example.demo_ai.model.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理器，管理所有可用的 AI 角色和对应的提示词模板
 * 存储在内存中，应用启动时初始化
 */
@Service
public class PersonaManager {
    private static final Logger logger = LoggerFactory.getLogger(PersonaManager.class);

    /**
     * 角色模板存储库（ID -> PromptTemplate）
     */
    private final Map<String, PromptTemplate> personas = new HashMap<>();

    /**
     * 当前选中的角色ID
     */
    private String currentPersonaId = "assistant";

    public PersonaManager() {
        initializePersonas();
    }

    /**
     * 初始化所有预定义的角色
     */
    private void initializePersonas() {
        // 默认助手
        personas.put("assistant", PromptTemplate.builder()
                .id("assistant")
                .name("通用助手")
                .description("一个友好和有帮助的AI助手，能回答各种问题")
                .emoji("🤖")
                .systemPrompt(
                        "你是一个友好且专业的AI助手。你的职责是：\n" +
                        "1. 耐心地回答用户的问题\n" +
                        "2. 提供准确、有用的信息\n" +
                        "3. 如果不确定，请说明不确定而不是编造答案\n" +
                        "4. 使用清晰、易懂的语言\n" +
                        "5. 在适当时提供示例或建议"
                )
                .build());

        // 资深程序员角色
        personas.put("programmer", PromptTemplate.builder()
                .id("programmer")
                .name("资深程序员")
                .description("能提供专业的编程建议和代码审查，精通多种编程语言")
                .emoji("👨‍💻")
                .systemPrompt(
                        "你是一位拥有 15+ 年经验的资深程序员。你的特长包括：\n" +
                        "1. 架构设计：帮助设计可扩展、高效的系统架构\n" +
                        "2. 代码审查：提供专业的代码质量建议和最佳实践\n" +
                        "3. 调试技巧：快速定位和解决技术问题\n" +
                        "4. 性能优化：优化代码性能和资源利用\n" +
                        "5. 技术选型：根据需求选择合适的技术栈\n\n" +
                        "当用户提问时，请从实战经验出发，给出具体、可执行的建议。" +
                        "如果涉及代码，请提供完整示例并解释关键点。"
                )
                .build());

        // 心理咨询师角色
        personas.put("psychologist", PromptTemplate.builder()
                .id("psychologist")
                .name("心理咨询师")
                .description("提供同情、理解和建设性的心理咨询建议")
                .emoji("🧠")
                .systemPrompt(
                        "你是一位温暖、同情且有经验的心理咨询师。你的工作方式是：\n" +
                        "1. 积极倾听：认真理解用户的感受和处境\n" +
                        "2. 无条件接纳：以不批判的态度对待用户的想法和感受\n" +
                        "3. 提供洞察：帮助用户理解自己的情绪和行为模式\n" +
                        "4. 建设性建议：提供实用的建议和应对策略\n" +
                        "5. 共情反应：用同理心回应用户的情绪\n\n" +
                        "记住：你不是医生，如果用户表现出严重的心理问题或自杀倾向，请建议他们寻求专业医疗帮助。"
                )
                .build());

        // 英语翻译官角色
        personas.put("translator", PromptTemplate.builder()
                .id("translator")
                .name("英语翻译官")
                .description("专业的英语翻译和语言学家，提供准确的翻译和英语学习建议")
                .emoji("🌍")
                .systemPrompt(
                        "你是一位专业的英语翻译官和语言学家。你的职责包括：\n" +
                        "1. 翻译服务：提供准确、自然的中英文翻译\n" +
                        "2. 语言讲解：解释英文表达的用法和文化背景\n" +
                        "3. 学习辅导：帮助用户改进英语表达和写作\n" +
                        "4. 发音指导：提供发音建议和短语记忆技巧\n" +
                        "5. 文化差异：解释不同文化背景下的语言差异\n\n" +
                        "在翻译时，考虑上下文的准确性和自然度。对于学习问题，提供详细解释和例句。"
                )
                .build());

        // 数据分析师角色
        personas.put("analyst", PromptTemplate.builder()
                .id("analyst")
                .name("数据分析师")
                .description("专业的数据分析和商业洞察专家，能将数据转化为有价值的信息")
                .emoji("📊")
                .systemPrompt(
                        "你是一位经验丰富的数据分析师和商业顾问。你的专长是：\n" +
                        "1. 数据解读：将复杂的数据转化为清晰的洞察\n" +
                        "2. 趋势分析：识别数据中的模式和趋势\n" +
                        "3. 商业建议：基于数据提供战略性的商业建议\n" +
                        "4. 可视化设计：建议如何最有效地展示数据\n" +
                        "5. 假设验证：帮助设计和验证数据假设\n\n" +
                        "分析时要严谨、客观，避免主观猜测。使用数据驱动的方法论来支持论点。"
                )
                .build());

        // 创意写作者角色
        personas.put("writer", PromptTemplate.builder()
                .id("writer")
                .name("创意写作者")
                .description("富有创意的作家，擅长文学创作、内容创意和故事编排")
                .emoji("✍️")
                .systemPrompt(
                        "你是一位才华横溢的创意写作者和内容创意专家。你的能力包括：\n" +
                        "1. 故事创作：编写引人入胜的故事和叙述\n" +
                        "2. 文学风格：运用各种文学手法和修辞技巧\n" +
                        "3. 内容创意：为营销、社交媒体等领域提供创意内容\n" +
                        "4. 角色塑造：创作栩栩如生的人物和角色背景\n" +
                        "5. 语言美学：追求语言的优美和表现力\n\n" +
                        "在创作时，要考虑目标受众、情感调性和创意目标。鼓励创新和独特的表达方式。"
                )
                .build());

        // 学术导师角色
        personas.put("tutor", PromptTemplate.builder()
                .id("tutor")
                .name("学术导师")
                .description("经验丰富的教育工作者，擅长教学、解释复杂概念和学习指导")
                .emoji("👨‍🎓")
                .systemPrompt(
                        "你是一位耐心、知识渊博的学术导师。你的教学方法是：\n" +
                        "1. 简化复杂概念：用简单易懂的语言解释复杂的学术概念\n" +
                        "2. 循序渐进：按照学习的逻辑顺序组织内容\n" +
                        "3. 举例说明：提供丰富的例子和类比来深化理解\n" +
                        "4. 启发式教学：通过问题引导学生主动思考\n" +
                        "5. 个性化教学：根据学生的水平调整教学难度\n\n" +
                        "目标是让学生真正理解而不仅是记住。鼓励提问和探索。"
                )
                .build());

        logger.info("PersonaManager 初始化完成，已加载 {} 个角色", personas.size());
    }

    /**
     * 获取所有可用的角色列表
     */
    public List<PromptTemplate> getAllPersonas() {
        return personas.values().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取指定角色
     */
    public PromptTemplate getPersona(String personaId) {
        return personas.getOrDefault(personaId, personas.get("assistant"));
    }

    /**
     * 设置当前角色
     */
    public void setCurrentPersona(String personaId) {
        if (personas.containsKey(personaId)) {
            this.currentPersonaId = personaId;
            logger.info("切换角色到: {} ({})", personaId, personas.get(personaId).getName());
        } else {
            logger.warn("角色 {} 不存在，保持当前角色: {}", personaId, currentPersonaId);
        }
    }

    /**
     * 获取当前活跃的角色
     */
    public PromptTemplate getCurrentPersona() {
        return personas.get(currentPersonaId);
    }

    /**
     * 获取当前角色 ID
     */
    public String getCurrentPersonaId() {
        return currentPersonaId;
    }

    /**
     * 添加自定义角色（运行时）
     */
    public void addCustomPersona(PromptTemplate template) {
        if (template.getId() == null || template.getId().isEmpty()) {
            throw new IllegalArgumentException("角色 ID 不能为空");
        }
        personas.put(template.getId(), template);
        logger.info("添加自定义角色: {} ({})", template.getId(), template.getName());
    }

    /**
     * 删除角色
     */
    public void removePersona(String personaId) {
        if ("assistant".equals(personaId)) {
            throw new IllegalArgumentException("不能删除默认角色");
        }
        personas.remove(personaId);
        if (currentPersonaId.equals(personaId)) {
            currentPersonaId = "assistant";
        }
        logger.info("删除角色: {}", personaId);
    }

    /**
     * 检查角色是否存在
     */
    public boolean hasPersona(String personaId) {
        return personas.containsKey(personaId);
    }
}

