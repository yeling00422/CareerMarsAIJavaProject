package com.example.careermarsaiproject.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.config.AiConfig;
import com.example.careermarsaiproject.config.AiJsonCleanerConfig;
//import com.example.careermarsaiproject.config.OpenAiConfig;
import com.example.careermarsaiproject.dto.AnalysisResultDto;
import com.example.careermarsaiproject.dto.CharacteristicsTestReportDto;
import com.example.careermarsaiproject.dto.RecommendationMentorDto;
import com.example.careermarsaiproject.entity.Job;
import com.example.careermarsaiproject.entity.Mentor;
import com.example.careermarsaiproject.entity.TestQuestion;
import com.example.careermarsaiproject.vo.*;
import com.fasterxml.jackson.core.type.TypeReference;
//import com.theokanning.openai.completion.chat.ChatCompletionRequest;
//import com.theokanning.openai.completion.chat.ChatCompletionResult;
//import com.theokanning.openai.completion.chat.ChatMessage;
//import com.theokanning.openai.completion.chat.ChatMessageRole;
//import com.theokanning.openai.service.OpenAiService;
import io.micrometer.common.util.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService2 {
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private IMentorService mentorService;
    @Autowired
    private IJobService jobService;
    @Autowired
    private ITestQuestionService testQuestionService;
//    @Autowired
//    private OpenAiService openAiService ;
//    @Autowired
//    private OpenAiConfig openAiConfig ;

    // 重试次数配置
    private static final int MAX_RETRY = 2;

    /**
     * 增强版AI调用方法 - 带重试机制
     */
    public String callWithMessage(String question) {
        // 拼接强制格式提示
        String finalQuestion = question + AiConfig.FORCE_PROMPT;

        // 重试机制
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                Generation gen = new Generation();
                Message systemMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("你是一个严格按照格式要求输出JSON的助手，只返回标准JSON，不添加任何额外内容")
                        .build();

                Message userMsg = Message.builder()
                        .role(Role.USER.getValue())
                        .content(finalQuestion)
                        .build();

                GenerationParam param = GenerationParam.builder()
                        .apiKey(aiConfig.getApiKey())
                        .model(aiConfig.getModel())
                        .messages(Arrays.asList(systemMsg, userMsg))
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .temperature(0.1F) // 降低随机性，提高格式稳定性
                        .build();

                String result = gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
                if (StringUtils.isNotBlank(result)) {
                    return result;
                }
            } catch (ApiException e) {
                System.err.println("AI调用异常（第" + (i+1) + "次）：" + e.getMessage());
                if (i == MAX_RETRY - 1) {
                    return "";
                }
                // 重试前短暂休眠
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                System.err.println("未知异常（第" + (i+1) + "次）：" + e.getMessage());
                if (i == MAX_RETRY - 1) {
                    return "";
                }
            }
        }
        return "";
    }

    public Result parseFile(MultipartFile file){
        try {
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            String fullText = "";

            if (fileName.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                    fullText = new PDFTextStripper().getText(doc);
                }
            } else if (fileName.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
                    doc.getTables().forEach(t -> t.getRows().forEach(r -> r.getTableCells().forEach(c -> sb.append(c.getText()).append(" "))));
                    fullText = sb.toString();
                }
            } else if (fileName.matches(".*\\.(jpg|jpeg|png)$")) {
                String base64Img = Base64.getEncoder().encodeToString(file.getBytes());
                String raw = HttpUtil.post("http://127.0.0.1:1224/api/ocr", JSONUtil.toJsonStr(Collections.singletonMap("base64", base64Img)));
                JSONArray data = JSONUtil.parseObj(raw).getJSONArray("data");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < data.size(); i++) {
                    sb.append(data.getJSONObject(i).getStr("text")).append("\n");
                }
                fullText = sb.toString();
            }

            if (StringUtils.isEmpty(fullText)){
                return Result.error("文件转文本失败！");
            }
            return Result.success(fullText);
        }catch (Exception e) {
            System.err.println("文件解析异常：" + e.getMessage());
            return Result.error("文件解析失败：" + e.getMessage());
        }
    }

    /**
     * 生成性格测试题 - 统一JSON处理
     */
    public Result<CharacteristicsTestListVo> generateCharacteristicsTest(String resumeText) {
        String ask = "这是当前求职者的简历信息:" + resumeText +
                "先判断这个信息是不是一份简历。如果不是简历返回{\"questions\":[]}。如果是简历，帮我根据简历生成十道性格测试题。格式如下：" +
                "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"title\": \"你在团队中通常扮演什么角色？\",\n" +
                "      \"options\": [\n" +
                "        {\n" +
                "          \"option\": \"A\",\n" +
                "          \"text\": \"领导者，负责统筹和决策\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"option\": \"B\",\n" +
                "          \"text\": \"协调者，促进沟通与合作\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"option\": \"C\",\n" +
                "          \"text\": \"执行者，专注完成任务\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"option\": \"D\",\n" +
                "          \"text\": \"创新者，提出新想法和方案\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            String result = callWithMessage(ask);
            // 提取并清洗JSON
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(result);
            if (cleanJson.isEmpty()) {
                return Result.error("测试题生成失败：返回格式不合法");
            }

            // 反序列化（带容错）
            Optional<CharacteristicsTestListVo> voOpt = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<CharacteristicsTestListVo>() {}
            );

            return voOpt.map(Result::success)
                    .orElseGet(() -> Result.success(new CharacteristicsTestListVo()));
        } catch (Exception e) {
            System.err.println("生成性格测试题异常：" + e.getMessage());
            return Result.success(new CharacteristicsTestListVo()); // 返回空对象而非错误
        }
    }

    /**
     * 生成性格测试报告 - 统一JSON处理
     */
    public Result<CharacteristicsTestReportVo> generateReport(CharacteristicsTestReportDto dto) {
        String ask = "这是性格测试题:" + dto.getCharacteristicsTest() +
                "这是求职者的答题结果:" + dto.getUserAnswers() +
                "这是求职者的简历文本:" + dto.getResumeText() +
                "根据这些信息帮我生成性格测试报告以及推荐几个岗位（岗位名称、推荐理由和岗位匹配程度 最高分100，最低分60）。格式如下：" +
                "{\n" +
                "  \"testReportText\": \"性格分析文本\",\n" +
                "  \"recommendedPositionList\": [\n" +
                "    {\n" +
                "      \"positionName\": \"金融分析师\",\n" +
                "      \"matchDegree\": \"92\",\n" +
                "      \"reasonsForRecommendation\": \"推荐理由\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            String result = callWithMessage(ask);
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(result);
            if (cleanJson.isEmpty()) {
                return Result.error("报告生成失败：返回格式不合法");
            }

            Optional<CharacteristicsTestReportVo> voOpt = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<CharacteristicsTestReportVo>() {}
            );

            return voOpt.map(Result::success)
                    .orElseGet(() -> Result.success(new CharacteristicsTestReportVo()));
        } catch (Exception e) {
            System.err.println("生成性格测试报告异常：" + e.getMessage());
            return Result.success(new CharacteristicsTestReportVo());
        }
    }

    /**
     * 简历匹配岗位 - 统一JSON处理
     */
    public Result<List<MatchJobVo>> matchJobByResumeText(String resumeText) {
        List<Job> jobList = jobService.list();
        Map<String, Job> jobMap = jobList.stream()
                .collect(Collectors.toMap(Job::getId, job -> job));

        List<JobVo> jobVoList = jobList.stream().map(job -> {
            JobVo vo = new JobVo();
            vo.setId(job.getId());
            vo.setCompany(job.getCompany());
            vo.setPosition(job.getPosition());
            vo.setLocations(job.getLocations());
            vo.setJdText(job.getJdText());
            return vo;
        }).collect(Collectors.toList());

        String ask = "这是当前求职者的简历信息:" + resumeText +
                "。这是当前数据库中所有岗位的信息:" + jobVoList +
                "。根据求职者简历匹配数据库中是否有合适的岗位。将合适的岗位id收集起来返回。如果数据库中没有合适的岗位就返回{\"jobids\":[]}。格式：" +
                "{\n" +
                "  \"jobids\": [\"11111\",\"22222\"]\n" +
                "}";

        try {
            String result = callWithMessage(ask);
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(result);
            if (cleanJson.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 解析jobids
            Map<String, List<String>> resultMap = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<Map<String, List<String>>>() {}
            ).orElse(Collections.emptyMap());

            List<String> jobIds = resultMap.getOrDefault("jobids", new ArrayList<>());
            List<MatchJobVo> voList = jobIds.stream()
                    .map(jobMap::get)
                    .filter(Objects::nonNull)
                    .map(job -> {
                        MatchJobVo vo = new MatchJobVo();
                        vo.setId(job.getId());
                        vo.setCompany(job.getCompany());
                        vo.setPosition(job.getPosition());
                        return vo;
                    }).collect(Collectors.toList());

            return Result.success(voList);
        } catch (Exception e) {
            System.err.println("岗位匹配异常：" + e.getMessage());
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 岗位有效性判断 - 增强鲁棒性
     */
    public Result judgmentPosition(String position) {
        String ask = "这是当前求职者想要寻找的岗位："+position+
                "判断这个岗位是否真实存在于市场中。只返回true或false，不要返回任何其他文字、标点或符号。";

        try {
            String result = callWithMessage(ask);
            if (StringUtils.isBlank(result)) {
                return Result.error("岗位验证失败！");
            }

            // 清洗结果，只保留true/false
            String cleanResult = result.trim().toLowerCase()
                    .replaceAll("[^a-z]", "")
                    .replace("true", "true")
                    .replace("false", "false");

            if ("true".equals(cleanResult)) {
                return Result.success(true);
            } else {
                return Result.error("岗位不存在！请输入正确的岗位信息！");
            }
        } catch (Exception e) {
            System.err.println("岗位判断异常：" + e.getMessage());
            return Result.error("岗位验证失败！");
        }
    }

    /**
     * 生成测试题 - 主方法
     */
    public Result<TestQuestionListVo> generateTestQuestion(String jobId, String position) {
        if (StringUtils.isEmpty(position)) {
            return Result.error("岗位信息不能为空！");
        }

        if (StringUtils.isEmpty(jobId)) {
            return aiGnederTestQuestion(position);
        } else {
            LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<TestQuestion>()
                    .eq(TestQuestion::getJobId, jobId)
                    .ne(TestQuestion::getType,5);
            List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);

            if (testQuestionList.size() >= 10) {
                TestQuestionListVo vo = new TestQuestionListVo();
                List<TestQuestionVo> testQuestionVoList = new ArrayList<>();

                for (TestQuestion testQuestion : testQuestionList) {
                    TestQuestionVo testQuestionVo = new TestQuestionVo();
                    List<TestQuestionOptionVo> options = new ArrayList<>();

                    // 优化选项构建逻辑
                    addOptionIfPresent(options, "A", testQuestion.getField1());
                    addOptionIfPresent(options, "B", testQuestion.getField2());
                    addOptionIfPresent(options, "C", testQuestion.getField3());
                    addOptionIfPresent(options, "D", testQuestion.getField4());
                    addOptionIfPresent(options, "E", testQuestion.getField5());
                    addOptionIfPresent(options, "F", testQuestion.getField6());

                    testQuestionVo.setName(testQuestion.getName());
                    testQuestionVo.setAnswer(testQuestion.getAnswer());
                    testQuestionVo.setType(testQuestion.getType());
                    testQuestionVo.setTitle(testQuestion.getTitle());
                    testQuestionVo.setOptions(options);
                    testQuestionVoList.add(testQuestionVo);
                }

                vo.setQuestions(testQuestionVoList);
                return Result.success(vo);
            } else {
                return aiGnederTestQuestion(position);
            }
        }
    }

    /**
     * 辅助方法：添加选项
     */
    private void addOptionIfPresent(List<TestQuestionOptionVo> options, String option, String text) {
        if (StringUtils.isNotBlank(text)) {
            TestQuestionOptionVo vo = new TestQuestionOptionVo();
            vo.setOption(option);
            vo.setText(text);
            options.add(vo);
        }
    }

    /**
     * AI生成测试题 - 统一JSON处理
     */
    public Result<TestQuestionListVo> aiGnederTestQuestion(String position) {
        String ask = "你是一位资深的 HR 和技术专家。请为岗位 [" + position + "] 生成10道模拟面试题。" +
                "要求：" +
                "1. 必须严格遵守以下 JSON 格式：" +
                "{\"questions\": [{\"title\":\"题目\",\"type\":1,\"options\":[{\"option\":\"A\",\"text\":\"选项内容\"}],\"answer\":\"A\"}]}" +
                "2. 禁止输出任何解释性文字、开场白或 Markdown 代码块标记" +
                "3. 确保 JSON 对象完整，不要遗漏任何键名";

        try {
            String result = callWithMessage(ask);
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(result);

            if (cleanJson.isEmpty()) {
                return Result.error("AI 生成的题目格式解析失败，请点击重试。");
            }

            TestQuestionListVo vo = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<TestQuestionListVo>() {}
            ).orElse(new TestQuestionListVo());

            if (vo.getQuestions() != null) {
                vo.getQuestions().forEach(question ->
                        question.setName(position + " 模拟测试题"));
            }

            return Result.success(vo);
        } catch (Exception e) {
            System.err.println("AI生成测试题异常：" + e.getMessage());
            return Result.error("处理异常: " + e.getMessage());
        }
    }

    /**
     * 分析结果 - 统一JSON处理
     */
    public Result<PerformanceAnalysis> analysisResult(AnalysisResultDto dto) {
        String ask = "根据以下信息生成求职分析报告：" +
                "格式要求：" +
                "{\n" +
                "  \"culturalCompatibility\" : 25,\n" +
                "  \"resumeMatchingScore\" : 25,\n" +
                "  \"overallPerformance\" : 36,\n" +
                "  \"improvements\" : [ {\n" +
                "    \"title\" : \"审计合规意识不足\",\n" +
                "    \"description\" : \"改进建议\"\n" +
                "  } ]\n" +
                "}" +
                "其中improvements为缺点以及建议(2-5条)。" +
                "学生简历：" + dto.getResumeText() +
                "期望岗位：" + dto.getPosition();

        try {
            String result = callWithMessage(ask);
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(result);

            if (cleanJson.isEmpty()) {
                return Result.error("解析结果失败：返回格式不合法");
            }

            PerformanceAnalysis analysis = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<PerformanceAnalysis>() {}
            ).orElse(new PerformanceAnalysis());

            return Result.success(analysis);
        } catch (Exception e) {
            System.err.println("分析结果异常：" + e.getMessage());
            return Result.error("解析结果失败！");
        }
    }

    /**
     * 推荐导师 - 统一JSON处理
     */
    public Result<EndResultVo> recommendationMentor(RecommendationMentorDto dto) {
        List<Mentor> mentorList = mentorService.list(new LambdaQueryWrapper<Mentor>()
                .eq(Mentor::getMenState, 3));

        List<MentorVo> voList = mentorList.stream().map(mentor -> {
            MentorVo vo = new MentorVo();
            BeanUtils.copyProperties(mentor, vo);
            return vo;
        }).collect(Collectors.toList());

        // 第一步：获取匹配的导师ID
        String ask = "这是学生的期望求职岗位：" + dto.getPosition() +
                "。这是所有导师的数据:" + voList +
                "帮我匹配合适的导师，仅返回导师id，用逗号隔开，最多十个。如果没有匹配的返回空字符串。";

        try {
            String result = callWithMessage(ask);
            if (StringUtils.isEmpty(result) || result.trim().equals("[]")) {
                return Result.error("导师推荐失败！");
            }

            // 清洗ID列表
            List<String> idList = Arrays.stream(result.replaceAll("[^0-9,]", "").split(","))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            if (idList.isEmpty()) {
                return Result.error("导师推荐失败！");
            }

            // 查询匹配的导师
            LambdaQueryWrapper<Mentor> queryWrapper = new LambdaQueryWrapper<Mentor>()
                    .in(Mentor::getId, idList);
            List<Mentor> endMentorList = mentorService.list(queryWrapper);

            if (endMentorList.isEmpty()) {
                return Result.error("导师推荐失败！");
            }

            // 第二步：生成详细推荐信息
            String endAsk = "这是学生的期望求职岗位：" + dto.getPosition() +
                    "。这是匹配的导师的数据:" + endMentorList +
                    "帮我生成推荐信息，格式如下：" +
                    "{\n" +
                    "  \"mentorList\": [\n" +
                    "    {\n" +
                    "      \"id\": 导师id,\n" +
                    "      \"marryRate\": 90,\n" +
                    "      \"menName\": \"David Chen\",\n" +
                    "      \"lableNames\": \"标签\",\n" +
                    "      \"rating\": 4.5,\n" +
                    "      \"studyCount\": 95,\n" +
                    "      \"successRate\": 95,\n" +
                    "      \"reasons\": [\"推荐理由1\",\"推荐理由2\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            String endResult = callWithMessage(endAsk);
            Optional<String> cleanJson = AiJsonCleanerConfig.extractJson(endResult);

            if (cleanJson.isEmpty()) {
                return Result.error("导师推荐失败！");
            }

            EndResultVo endResultVo = AiJsonCleanerConfig.parseJson(
                    cleanJson.get(),
                    new TypeReference<EndResultVo>() {}
            ).orElse(new EndResultVo());

            return Result.success(endResultVo);
        } catch (Exception e) {
            System.err.println("推荐导师异常：" + e.getMessage());
            return Result.error("导师推荐失败");
        }
    }

    /**
     * OpenAI调用方法
     */
//    public String callWithMessage2(String question) {
//        try {
//            ChatMessage systemMsg = new ChatMessage(
//                    ChatMessageRole.SYSTEM.value(),
//                    "You are a helpful assistant that only returns standard JSON format."
//            );
//
//            ChatMessage userMsg = new ChatMessage(
//                    ChatMessageRole.USER.value(),
//                    question + AiConfig.FORCE_PROMPT
//            );
//
//            ChatCompletionRequest request = ChatCompletionRequest.builder()
//                    .model(openAiConfig.getModel())
//                    .messages(Arrays.asList(systemMsg, userMsg))
//                    .temperature(0.1)
//                    .build();
//
//            ChatCompletionResult result = openAiService.createChatCompletion(request);
//            return result.getChoices().get(0).getMessage().getContent();
//        } catch (Exception e) {
//            System.err.println("OpenAI调用异常：" + e.getMessage());
//            return "";
//        }
//    }
}