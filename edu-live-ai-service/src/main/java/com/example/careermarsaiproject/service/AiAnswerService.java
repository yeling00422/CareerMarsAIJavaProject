package com.example.careermarsaiproject.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.config.AiConfig;
import com.example.careermarsaiproject.config.AiJsonCleanerConfig;
import com.example.careermarsaiproject.dto.AnalysisResultDto;
import com.example.careermarsaiproject.dto.RecommendationMentorDto;
import com.example.careermarsaiproject.entity.*;
import com.example.careermarsaiproject.utils.IdWorker;
import com.example.careermarsaiproject.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiAnswerService {
    // 全局复用客户端（避免重复创建，性能提升巨大）
    private final Generation gen = new Generation();
    @Autowired
    private AiConfig aiConfig;
    @Autowired
    private IMentorService mentorService;
    @Autowired
    private IJobService jobService;
    @Autowired
    private ITestQuestionService testQuestionService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IMbtiQuestionService mbtiQuestionService;
    @Autowired
    private IConstellationFoundationScoreService constellationFoundationScoreService;
    @Autowired
    private IMbtiResultService mbtiResultService;
    @Autowired
    private IConsultationRecordService consultationRecordService;
    @Autowired
    private TencentOcrService tencentOcrService;

    // ====================== 统一AI调用核心 ======================
    public String callWithMessage(String question, int maxRetry, int timeOut) throws Exception {
        String finalQuestion = question + AiConfig.FORCE_PROMPT;

        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是严格按照格式要求输出JSON的助手，只返回标准JSON，不添加任何额外内容")
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
                .temperature(0.1F)
                .build();

        Exception lastException = null;
        for (int i = 0; i < maxRetry; i++) {
            CompletableFuture<String> future = null;
            try {
                future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                String result = future.get(timeOut, TimeUnit.MILLISECONDS);
                return AiJsonCleanerConfig.cleanJson(result);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.warn("AI调用超时，第 {}/{} 次", i + 1, maxRetry);
                lastException = e;
            } catch (java.util.concurrent.ExecutionException e) {
                // 解包真实异常：ExecutionException -> RuntimeException -> 原始异常
                Throwable root = e.getCause() != null ? e.getCause().getCause() : null;
                if (root instanceof NoApiKeyException || root instanceof InputRequiredException) {
                    log.error("AI鉴权或参数错误，终止重试：{}", root.getMessage());
                    throw (Exception) root;
                }
                log.warn("AI调用失败，第 {}/{} 次，错误：{}", i + 1, maxRetry, e.getMessage());
                lastException = e;
            } catch (Exception e) {
                log.warn("AI调用异常，第 {}/{} 次，错误：{}", i + 1, maxRetry, e.getMessage());
                lastException = e;
            }
            // 指数退避：500ms -> 1000ms -> 2000ms
            if (i < maxRetry - 1) {
                long backoff = Math.min(500L * (1L << i), 2000L);
                log.info("等待 {}ms 后重试", backoff);
                Thread.sleep(backoff);
            }
        }
        log.error("AI调用全部重试失败，共 {} 次", maxRetry);
        throw new RuntimeException("AI服务暂时不可用，请稍后重试", lastException);
    }

    public Result parseFile(MultipartFile file) {
        try {
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            log.info("开始解析文件: {}", fileName);
            String fullText = "";
            if (fileName.endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                    fullText = new PDFTextStripper().getText(doc);
                }
            } else if (fileName.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
                    doc.getTables().forEach(t -> t.getRows().forEach(r ->
                            r.getTableCells().forEach(c -> sb.append(c.getText()).append(" "))));
                    fullText = sb.toString();
                }
            } else if (fileName.matches(".*\\.(jpg|jpeg|png)$")) {
                // 直接调用腾讯云OCR，抛弃本地tesseract
                fullText = tencentOcrService.ocrImage(file);
            }
            if (StringUtils.isEmpty(fullText)) {
                log.warn("文件解析结果为空: {}", fileName);
                return Result.error("文件转文本失败！");
            }
            log.info("文件解析成功: {}, 文本长度: {}", fileName, fullText.length());
            return parseResumeText(fullText);
        } catch (Exception e) {
            log.error("文件解析失败: {}", e.getMessage(), e);
            return Result.error("文件解析失败：" + e.getMessage());
        }
    }


    public Result<ResumeVo> parseResumeText(String resumeText) {
        String ask = "这是是当前求职者的简历信息:" + resumeText + "解析简历中的内容。返回我需要的信息。" +
                "其中。name为姓名。date为出生日期（需要年月日）。educationalQualifications为最高学历。school为毕业院校（最后的毕业院校）。educationalTime为毕业时间（仅需要年和月）。skill为专业技能。educationalExperience和jobExperience教育经历和工作经历"+
                "如果没有识别出来改字段就返回空字符串即可。格式如下：" +
                "{\n" +
                "  \"name\": \"张三\",\n" +
                "  \"date\": \"2000-01-01\",\n" +
                "  \"educationalQualifications\": \"本科\",\n" +
                "  \"school\": \"南京理工大学\",\n" +
                "  \"educationalTime\": \"2022-06\",\n" +
                "  \"skill\": \"Java、python\",\n" +
                "  \"educationalExperience\": \"2018-09 至 2022-06 南京理工大学 计算机专业\",\n" +
                "  \"jobExperience\": \"2022-07 至今 腾讯科技有限公司 后端开发工程师\"\n" +
                "}";
        try {
            String result = callWithMessage(ask, 2,30000);
            // 1. 判断 null / 空串
            if (result == null || result.isBlank()) {
                log.error("AI 返回空内容");
                return Result.error("简历解析失败！");
            }

            // 2. 判断空 JSON（无效返回）
            String cleanResult = result.trim();
            if (cleanResult.equals("{}") || cleanResult.equals("[]")) {
                log.error("AI 返回空 JSON：{}", result);
                return Result.error("简历解析失败！");
            }

            // 3. 正常解析
            ResumeVo vo = objectMapper.readValue(result, new TypeReference<ResumeVo>() {});
            return Result.success(vo);

        } catch (Exception e) {
            log.error("简历解析失败！", e);
        }
        return Result.error("简历解析失败！");
    }

    public Result<MBTIContentVo> searchMBTIContent(String constellation) {
        try {
            ConstellationFoundationScore constellationFoundationScore = constellationFoundationScoreService.getOne(new LambdaQueryWrapper<ConstellationFoundationScore>()
                    .eq(ConstellationFoundationScore::getConstellation, constellation));

            if (constellationFoundationScore == null){
                return Result.error("未查询到星座！出生日期可能有问题！");
            }

//            LambdaQueryWrapper<MbtiQuestion> q001 = new LambdaQueryWrapper<MbtiQuestion>().eq(MbtiQuestion::getId, "q001");
            List<MbtiQuestion> mbtiQuestionList = mbtiQuestionService.list();
            if (mbtiQuestionList.size() == 0){
                return Result.error("未查询到MBTI测试题！");
            }

            MBTIContentVo vo = new MBTIContentVo();
            vo.setConstellationFoundationScore(constellationFoundationScore);
            vo.setMbtiQuestionList(mbtiQuestionList);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("MBTI测试题查询失败！", e);
        }
        return Result.error("MBTI测试题查询失败！");
    }

    public Result<MbtiResult> searchMBTIResult(String name) {
        try {
            MbtiResult mbtiResult = mbtiResultService.getOne(new LambdaQueryWrapper<MbtiResult>()
                    .eq(MbtiResult::getName, name));


            if (mbtiResult == null){
                return Result.error("未查询对应的人格结果！");
            }

            return Result.success(mbtiResult);
        } catch (Exception e) {
            log.error("MBTI测试结果查询失败！", e);
        }
        return Result.error("MBTI测试结果查询失败！");
    }



    public Result<List<MatchJobVo>> matchJobByResumeText(String resumeText) {
        log.info("开始匹配岗位");
        List<Job> jobList = jobService.list();
        Map<String, Job> jobMap = jobList.stream().collect(Collectors.toMap(Job::getId, job -> job));
        List<JobVo> jobVoList = jobList.stream().map(job -> {
            JobVo vo = new JobVo();
            vo.setId(job.getId());
            vo.setCompany(job.getCompany());
            vo.setPosition(job.getPosition());
            vo.setLocations(job.getLocations());
            vo.setJdText(job.getJdText());
            return vo;
        }).collect(Collectors.toList());

        List<MatchJobVo> voList = new ArrayList<>();
        String ask;
        try {
            ask = "求职者简历:" + resumeText
                    + " 所有岗位:" + objectMapper.writeValueAsString(jobVoList)
                    + " 匹配简历中合适的岗位,返回岗位id(最多30个),无合适岗位返回空数组。严格按以下JSON格式返回:"
                    + "{\"jobids\":[\"11111\",\"22222\"]}";
        } catch (JsonProcessingException e) {
            log.error("匹配岗位失败!", e);
            return Result.success(voList);
        }

        try {
            String result = callWithMessage(ask, 3, 20000);
            if (result == null || result.isBlank()) {
                log.warn("匹配岗位: AI返回内容为空");
                return Result.success(voList);
            }
            List<String> jobIds = objectMapper.readValue(
                    objectMapper.readTree(result).get("jobids").toString(), List.class);
            if (jobIds == null || jobIds.isEmpty()) {
                log.info("匹配岗位: 未找到合适岗位");
                return Result.success(voList);
            }
            for (String jobId : jobIds) {
                Job job = jobMap.get(jobId);
                if (job == null) continue;
                MatchJobVo vo = new MatchJobVo();
                vo.setId(job.getId());
                vo.setCompany(job.getCompany());
                vo.setPosition(job.getPosition());
                voList.add(vo);
            }
            log.info("岗位匹配成功, 匹配数量: {}", voList.size());
            return Result.success(voList);
        } catch (Exception e) {
            log.error("匹配岗位失败!", e);
        }
        return Result.success(voList);
    }


    public Result judgmentResume(String resumeText) {
        log.info("开始校验简历");
        String ask = "判断以下文本是否是一份简历,是则返回true,否则返回false。文本:" + resumeText;
        try {
            String result = callWithMessage(ask, 3, 20000);
            if (result != null && result.contains("true")) {
                log.info("简历校验通过");
                return Result.success(result);
            }
            log.warn("简历校验不通过: 内容不是简历格式");
            return Result.error("请输入正确的简历信息！");
        } catch (Exception e) {
            log.error("简历校验异常: {}", e.getMessage(), e);
            return Result.error("请输入正确的简历信息！");
        }
    }

    public Result judgmentPosition(String position) {
        log.info("开始校验岗位: {}", position);
        String ask = "判断以下岗位市面上是否存在,存在返回true,不存在返回false。岗位:" + position;
        try {
            String result = callWithMessage(ask, 3, 20000);
            if (result != null && result.contains("true")) {
                log.info("岗位校验通过: {}", position);
                return Result.success(result);
            }
            log.warn("岗位不存在: {}", position);
            return Result.error("岗位不存在！请输入正确的岗位信息！");
        } catch (Exception e) {
            log.error("岗位校验异常: {}", e.getMessage(), e);
            return Result.error("请输入正确的岗位名称！");
        }
    }

    public Result<PersonalAbilityImgVo> generatePersonalAbility(String resumeText) {
        log.info("开始生成个人能力图");
        String ask = "简历:" + resumeText
                + " 根据简历提取6个能力维度并评分(0-100)。每项能力的得分数字不要太接近，这是个六边形能力图。不要六边形战士！严格按以下JSON格式返回,personalAbility数组必须包含6项:"
                + "{\"personalAbility\":[{\"ability\":\"能力名称\",\"score\":\"85\"}"
                + ",{\"ability\":\"能力名称\",\"score\":\"90\"}]}";
        try {
            String result = callWithMessage(ask, 3, 20000);
            if (result == null || result.isBlank()) {
                log.warn("个人能力图生成失败: AI返回内容为空");
                return emptyAbilityResult();
            }
            PersonalAbilityImgVo vo = objectMapper.readValue(result, new TypeReference<PersonalAbilityImgVo>() {});
            if (vo == null || vo.getPersonalAbility() == null || vo.getPersonalAbility().size() != 6) {
                log.warn("个人能力图生成失败: 数据不完整");
                return emptyAbilityResult();
            }
            log.info("个人能力图生成成功");
            return Result.success(vo);
        } catch (Exception e) {
            log.error("个人能力图生成失败", e);
            return emptyAbilityResult();
        }
    }

    private Result<PersonalAbilityImgVo> emptyAbilityResult() {
        PersonalAbilityImgVo emptyVo = new PersonalAbilityImgVo();
        emptyVo.setPersonalAbility(Collections.emptyList());
        return Result.success(emptyVo);
    }

    public Result<TestQuestionListVo> generateTestQuestion(String jobId, String position) {
        log.info("开始生成测试题, jobId: {}, position: {}", jobId, position);
        if (StringUtils.isEmpty(position)) {
            return Result.error("岗位信息不能为空！");
        }
        if (StringUtils.isEmpty(jobId)) {
            return aiGnederTestQuestion(position);
        }
        LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getJobId, jobId).ne(TestQuestion::getType, 5);
        List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);
        if (testQuestionList.size() >= 10) {
            log.info("从数据库获取测试题成功, 数量: {}", testQuestionList.size());
            return buildTestQuestionVo(testQuestionList);
        }
        log.info("数据库题目不足({} 题), 调用AI生成", testQuestionList.size());
        return aiGnederTestQuestion(position);
    }

    private Result<TestQuestionListVo> buildTestQuestionVo(List<TestQuestion> testQuestionList) {
        TestQuestionListVo vo = new TestQuestionListVo();
        List<TestQuestionVo> testQuestionVoList = new ArrayList<>();

        // 1. 复制原集合，防止打乱原数据顺序
        List<TestQuestion> tempList = new ArrayList<>(testQuestionList);
        // 2. 随机打乱
        Collections.shuffle(tempList);
        // 3. 最多取10条，不足10条取全部
        int takeNum = Math.min(tempList.size(), 10);
        List<TestQuestion> randomTen = tempList.subList(0, takeNum);

        for (TestQuestion tq : randomTen) {
            TestQuestionVo tqVo = new TestQuestionVo();
            List<TestQuestionOptionVo> options = new ArrayList<>();
            addOption(options, "A", tq.getField1());
            addOption(options, "B", tq.getField2());
            addOption(options, "C", tq.getField3());
            addOption(options, "D", tq.getField4());
            addOption(options, "E", tq.getField5());
            addOption(options, "F", tq.getField6());
            tqVo.setName(tq.getName());
            tqVo.setAnswer(tq.getAnswer());
            tqVo.setType(tq.getType());
            tqVo.setTitle(tq.getTitle());
            tqVo.setOptions(options);
            testQuestionVoList.add(tqVo);
        }
        vo.setQuestions(testQuestionVoList);
        return Result.success(vo);
    }

    private void addOption(List<TestQuestionOptionVo> options, String label, String text) {
        if (!StringUtils.isEmpty(text)) {
            TestQuestionOptionVo opt = new TestQuestionOptionVo();
            opt.setOption(label);
            opt.setText(text);
            options.add(opt);
        }
    }

    public Result<TestQuestionListVo> aiGnederTestQuestion(String position) {
        log.info("AI开始生成测试题, position: {}", position);
        String ask = "岗位:" + position
                + " 生成10道该岗位专属测试题,每题含ABCD四个选项和正确答案,type固定为1。严格按以下JSON格式返回,questions数组共10项:"
                + "{\"questions\":[{\"title\":\"题目内容\",\"type\":1,\"options\":"
                + "[{\"option\":\"A\",\"text\":\"选项A\"},{\"option\":\"B\",\"text\":\"选项B\"}"
                + ",{\"option\":\"C\",\"text\":\"选项C\"},{\"option\":\"D\",\"text\":\"选项D\"}]"
                + ",\"answer\":\"A\"}]}";
        try {
            String result = callWithMessage(ask, 2, 30000);
            if (result == null || result.isBlank()) {
                log.warn("AI生成测试题失败: 返回内容为空");
                return Result.error("测试问题生成失败！");
            }
            TestQuestionListVo vo = objectMapper.readValue(result, new TypeReference<TestQuestionListVo>() {});
            if (vo == null) {
                log.warn("AI生成测试题失败: 解析结果为null");
                return Result.error("测试问题生成失败！");
            }
            vo.getQuestions().forEach(q -> q.setName(position + "行业/岗位笔试题"));
            log.info("AI生成测试题成功");
            return Result.success(vo);
        } catch (Exception e) {
            log.error("测试问题生成失败", e);
        }
        return Result.error("测试问题生成失败！");
    }


    public Result<PerformanceAnalysis> analysisResult(AnalysisResultDto dto) {
        log.info("开始综合测评分析, position: {}", dto.getPosition());
        String ask = "学生简历:" + dto.getResumeText()
                + " 期望岗位:" + dto.getPosition()
                + " 根据简历和岗位动态生成分析结果,所有分值必须基于实际内容计算。"
                + "culturalCompatibility为学历与岗位匹配得分(0-100纯数字),"
                + "resumeMatchingScore为简历与岗位匹配得分(0-100纯数字),"
                + "overallPerformance为超越同岗位求职者的百分比(0-100纯数字),"
                + "improvements为2-5条改进建议。严格按以下JSON格式返回:"
                + "{\"culturalCompatibility\":0,\"resumeMatchingScore\":0,\"overallPerformance\":0,"
                + "\"improvements\":[{\"title\":\"改进点标题\",\"description\":\"详细建议\"}]}";
        try {
            String result = callWithMessage(ask, 3, 20000);
            if (result == null || result.isBlank()) {
                log.warn("综合测评分析失败: AI返回内容为空");
                return Result.error("解析结果失败！");
            }
            PerformanceAnalysis performanceAnalysis = objectMapper.readValue(result, new TypeReference<PerformanceAnalysis>() {});
            log.info("综合测评分析成功");
            return Result.success(performanceAnalysis);
        } catch (Exception e) {
            log.error("解析结果失败!", e);
        }
        return Result.error("解析结果失败！");
    }

    public Result<EndResultVo> recommendationMentor(RecommendationMentorDto dto) {
        log.info("开始导师推荐, position: {}", dto.getPosition());
        List<Mentor> mentorList = mentorService.list(new LambdaQueryWrapper<Mentor>().eq(Mentor::getMenState,3));
        if (mentorList.size() == 0) {
            log.warn("暂无可用导师");
            return Result.error("暂无可用导师");
        }
        List<MentorVo> voList = new ArrayList<>();
        for (Mentor mentor : mentorList) {
            MentorVo vo = new MentorVo();
            BeanUtils.copyProperties(mentor, vo);
            voList.add(vo);
        }

        String ask = "这是学生的期望求职岗位：" + dto.getPosition() + "。这是数据库中的导师数据:" + voList + "帮我生成这样的对象返回，marryRate匹配率，successRate为成功功率，rating为导师评分，reasons为推荐理由，这几个参数帮我动态生成，其他从数据中拿就好，以下是我需要的格式" +
                "{\n" +
                "  \"mentorList\": [\n" +
                "    {\n" +
                "      \"id\": 导师id,\n" +
                "      \"marryRate\": 90,\n" +
                "      \"menName\": \"David Chen\",\n" +
                "      \"lableNames\": \"Ex-Google PM Career Coach | Behavioral lnterview Specialist\",\n" +
                "      \"rating\": 4.5,\n" +
                "      \"studyCount\": 95,\n" +
                "      \"successRate\": 95,\n" +
                "      \"reasons\": [\n" +
                "        \"精通审计合规与风险报告机制，可针对性解决审计意识不足问题\",\n" +
                "        \"具备集团及审计制度设计经验，与目标岗位高度匹配\",\n" +
                "        \"擅长系统性思维训练，能强化对审计闭环管理的理解\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 导师id,\n" +
                "      \"marryRate\": 85,\n" +
                "      \"menName\": \"Emily Wang\",\n" +
                "      \"lableNames\": \"Former McKinsey Consultant | Case Interview Expert\",\n" +
                "      \"rating\": 4.2,\n" +
                "      \"studyCount\": 78,\n" +
                "      \"successRate\": 88,\n" +
                "      \"reasons\": [\n" +
                "        \"拥有顶级咨询公司背景，深谙案例面试套路\",\n" +
                "        \"擅长结构化思维训练，快速提升解题能力\",\n" +
                "        \"熟悉各行业商业模型，提供实战性建议\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        try {
            String endResult = callWithMessage(ask,3,20000);
//            String endResult = "{\n" +
//                    "  \"mentorList\" : [ {\n" +
//                    "    \"id\" : 1740408505084284929,\n" +
//                    "    \"marryRate\" : 88,\n" +
//                    "    \"menName\" : \"Sam\",\n" +
//                    "    \"lableNames\" : \"面试辅导,职业规划,项目管理,零售\",\n" +
//                    "    \"rating\" : 4.3,\n" +
//                    "    \"studyCount\" : 0,\n" +
//                    "    \"successRate\" : 86,\n" +
//                    "    \"reasons\" : [ \"具备零售行业实战经验，与外贸业务员岗位有较强关联性\", \"擅长职业规划与项目管理，能有效指导求职路径设计\", \"拥有外企工作经验，熟悉国际化工作环境与沟通方式\" ]\n" +
//                    "  }, {\n" +
//                    "    \"id\" : 1743185570602835970,\n" +
//                    "    \"marryRate\" : 92,\n" +
//                    "    \"menName\" : \"吴宇\",\n" +
//                    "    \"lableNames\" : \"金融,券商行业,投资银行,面试辅导\",\n" +
//                    "    \"rating\" : 4.6,\n" +
//                    "    \"studyCount\" : null,\n" +
//                    "    \"successRate\" : 90,\n" +
//                    "    \"reasons\" : [ \"具备金融与投资银行背景，熟悉国际业务运作模式\", \"擅长面试辅导，能针对性提升外贸岗位应聘能力\", \"有券商行业经验，对跨境交易与客户沟通有深入理解\" ]\n" +
//                    "  }, {\n" +
//                    "    \"id\" : 1754032332653707265,\n" +
//                    "    \"marryRate\" : 85,\n" +
//                    "    \"menName\" : \"Shum\",\n" +
//                    "    \"lableNames\" : \"金融,投资银行,职业规划,高技术产业（制造业）,ESG投资,面试辅导\",\n" +
//                    "    \"rating\" : 4.7,\n" +
//                    "    \"studyCount\" : null,\n" +
//                    "    \"successRate\" : 93,\n" +
//                    "    \"reasons\" : [ \"拥有头部PE及投资银行背景，具备国际化视野\", \"熟悉高技术制造业，与外贸涉及的工业品出口高度相关\", \"擅长职业规划与面试辅导，能系统性提升求职竞争力\" ]\n" +
//                    "  }, {\n" +
//                    "    \"id\" : 1744252733132529666,\n" +
//                    "    \"marryRate\" : 87,\n" +
//                    "    \"menName\" : \"Sherman\",\n" +
//                    "    \"lableNames\" : \"金融,咨询,面试辅导,职业规划\",\n" +
//                    "    \"rating\" : 4.4,\n" +
//                    "    \"studyCount\" : 19,\n" +
//                    "    \"successRate\" : 89,\n" +
//                    "    \"reasons\" : [ \"麦肯锡背景提供顶级咨询思维训练，强化问题解决能力\", \"兼具金融与咨询经验，适合培养外贸业务中的商业敏感度\", \"擅长面试辅导与职业规划，助力快速定位目标岗位\" ]\n" +
//                    "  }, {\n" +
//                    "    \"id\" : 1794990874453508097,\n" +
//                    "    \"marryRate\" : 84,\n" +
//                    "    \"menName\" : \"Robert\",\n" +
//                    "    \"lableNames\" : \"金融,投资银行,面试辅导,职业规划,银行,行业研究\",\n" +
//                    "    \"rating\" : 4.5,\n" +
//                    "    \"studyCount\" : null,\n" +
//                    "    \"successRate\" : 87,\n" +
//                    "    \"reasons\" : [ \"具备投资银行与行业研究经验，有助于理解外贸市场动态\", \"熟悉银行体系运作，对国际贸易结算流程有实际认知\", \"长期从事面试辅导，能精准提升应聘成功率\" ]\n" +
//                    "  } ]\n" +
//                    "}";
            if (StringUtils.isEmpty(endResult)) {
                log.warn("导师推荐失败: AI返回内容为空");
                return  Result.error("导师推荐失败！");
            }
            // 将 JSON 字符串转换为 List<MatchPositionVo>
            EndResultVo endResultVo = objectMapper.readValue(endResult,
                    new TypeReference<EndResultVo>() {
                    });
            List<MentorResultVo> recommendationList = endResultVo.getMentorList();
            List<MentorResultVo> mentorResultVos = selectMentorByPositionAndMBTIResult(mentorList, dto.getPosition(), dto.getMbtiResult());
            if (recommendationList.size() == 0){
                if (mentorResultVos.size() != 0){
                    endResultVo.setMentorList(mentorResultVos);
                    return Result.success(endResultVo);
                }else{
                    return Result.error("导师推荐失败！");
                }
            }else{
                List<String> existMentorIdList = recommendationList.stream()
                        .map(entity -> entity.getId()) // 映射取出id
                        .collect(Collectors.toList());

                List<MentorResultVo> addNewMentors = mentorResultVos.stream()
                        .filter(vo -> !existMentorIdList.contains(vo.getId()))
                        .collect(Collectors.toList());
                recommendationList.addAll(addNewMentors);

                // 赋值完整合并后的列表（核心修复点：使用recommendationList，不是mentorResultVos）
                endResultVo.setMentorList(recommendationList);
                return Result.success(endResultVo);
            }
        } catch (Exception e) {
            log.error("导师推荐失败!",e);
        }
        return Result.error("导师推荐失败!");
    }


    public List<MentorResultVo> selectMentorByPositionAndMBTIResult(List<Mentor> mentorVoList,String position, String mbtiName) {
        LambdaQueryWrapper<MbtiResult> mbtiResultLambdaQueryWrapper = new LambdaQueryWrapper<>();
        mbtiResultLambdaQueryWrapper.eq(MbtiResult::getName,mbtiName);
        MbtiResult mbtiResult = mbtiResultService.getOne(mbtiResultLambdaQueryWrapper);
        String firstIndustry = mbtiResult.getFirstIndustry();
        String firstPostion = mbtiResult.getFirstPostion();
        String secondIndustry = mbtiResult.getSecondIndustry();
        String secondPostion = mbtiResult.getSecondPostion();
        List<MentorResultVo> voList = new ArrayList<>();
        for (Mentor mentor : mentorVoList) {
            MentorResultVo vo = new MentorResultVo();
            String lableNames = mentor.getLableNames();
            List<String> reasons = new ArrayList<>();
            boolean matchFirstIndustry = hasMatchLabel(lableNames, firstIndustry);
            boolean matchSecondIndustry = hasMatchLabel(lableNames, secondIndustry);
            boolean matchFirstPosition = hasMatchLabel(lableNames, firstPostion);
            boolean matchSecondPosition = hasMatchLabel(lableNames, secondPostion);
            boolean matchUserPosition = hasMatchLabel(lableNames, position);

            if (matchFirstIndustry || matchSecondIndustry || matchFirstPosition || matchSecondPosition || matchUserPosition){
                BeanUtils.copyProperties(mentor,vo);
                vo.setName(mentor.getMenName());
                //marryRate匹配率，successRate为成功功率，rating为导师评分，reasons为推荐理由
                int marryRate = ThreadLocalRandom.current().nextInt(60, 101);
                int placementRate = ThreadLocalRandom.current().nextInt(60, 101);
                // 保留1位小数示例
                double randomDouble = ThreadLocalRandom.current().nextDouble(3.0, 5.01);
                double rating = Math.round(randomDouble * 10.0) / 10.0;

                vo.setMarryRate(marryRate);
                vo.setRating(rating);
                vo.setPlacementRate(placementRate);
                if (matchFirstIndustry){
                    reasons.add(mbtiName+"人格首选推荐行业");
                }
                if (matchSecondIndustry){
                    reasons.add(mbtiName+"人格次选推荐行业");
                }
                if (matchFirstPosition){
                    reasons.add(mbtiName+"人格首选推荐岗位");
                }
                if (matchSecondPosition){
                    reasons.add(mbtiName+"人格次选推荐岗位");
                }
                if (matchUserPosition){
                    reasons.add("与您选择/填写的“"+position+"”高度匹配");
                }
                vo.setReasons(reasons);
                voList.add(vo);
            }
        }
        return voList;
    }

    /**
     * 判断两组标签是否存在匹配项
     * @param labels1 导师标签串，逗号分隔
     * @param labels2 MBTI标签串，顿号分隔
     * @return true=存在匹配标签
     */
    private boolean hasMatchLabel(String labels1, String labels2) {
        if (StrUtil.isBlank(labels1) || StrUtil.isBlank(labels2)) {
            return false;
        }
        // 拆分两边标签
        List<String> mentorLabels = Arrays.stream(labels1.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .toList();
        List<String> mbtiLabels = Arrays.stream(labels2.split("、"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .toList();
        // 任意标签互相包含即匹配
        for (String mLabel : mentorLabels) {
            for (String mbLabel : mbtiLabels) {
                if (mLabel.contains(mbLabel) || mbLabel.contains(mLabel)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional()
    public Result<EndResultVo> saveConsultationRecord(String studentId, String mentorId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ConsultationRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsultationRecord::getStudentId,studentId);
        queryWrapper.eq(ConsultationRecord::getMentorId,mentorId);
        ConsultationRecord consultationRecord = consultationRecordService.getOne(queryWrapper);
        if (consultationRecord != null){
            //判断今天是否已经保存了一条记录
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime lastUpdateTime = consultationRecord.getUpdateTime();
            if (lastUpdateTime.isAfter(todayStart)) {
                //今天已经提交过记录了
                return Result.error("今天已经提交过咨询记录了哦！");
            } else {
                // 不是今天，更新最新的记录与时间
                consultationRecord.setTotalCount(consultationRecord.getTotalCount()+1);
                consultationRecord.setUpdateTime(now);
                consultationRecordService.updateById(consultationRecord);
            }
        }else {
            consultationRecord = new ConsultationRecord();
            consultationRecord.setId(IdWorker.getId().toString());
            consultationRecord.setStudentId(studentId);
            consultationRecord.setMentorId(mentorId);
            consultationRecord.setCreateTime(now);
            consultationRecord.setUpdateTime(now);
            consultationRecord.setHandelStatus(0);
            consultationRecord.setTotalCount(1);
            consultationRecordService.save(consultationRecord);
        }
        return Result.success();
    }

    public Result<EndResultVo> searchAllMentor() {
        List<Mentor> mentorList = mentorService.list(new LambdaQueryWrapper<Mentor>().eq(Mentor::getMenState,3));
        if (mentorList.size() == 0) {
            log.warn("暂无可用导师");
            return Result.error("暂无可用导师");
        }
        List<MentorResultVo> voList = new ArrayList<>();
        for (Mentor mentor : mentorList) {
            MentorResultVo vo = new MentorResultVo();//MentorResultVo
            BeanUtils.copyProperties(mentor, vo);
            vo.setName(mentor.getMenName());
            vo.setLevel(mentor.getLevel());
            vo.setResume(mentor.getIntro());
            voList.add(vo);
        }
        EndResultVo endResultVo = new EndResultVo();
        endResultVo.setMentorList(voList);
        return Result.success(endResultVo);
    }
}