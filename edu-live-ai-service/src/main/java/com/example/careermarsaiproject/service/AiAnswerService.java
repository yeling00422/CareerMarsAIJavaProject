package com.example.careermarsaiproject.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
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

    // ====================== 统一AI调用核心 ======================
    public String callWithMessage(String question,int maxRetry,int timeOut) throws Exception {
        String finalQuestion = question + AiConfig.FORCE_PROMPT;
        timeOut = timeOut;

        // 构建请求参数（只构建一次）
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

        for (int i = 0; i < maxRetry; i++) {
            try {
                // 带超时执行
                String result = CompletableFuture.supplyAsync(() -> {
                    try {
                        return gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).get(timeOut, TimeUnit.MILLISECONDS);
                // JSON清洗（解决99%格式错误）
                return AiJsonCleanerConfig.cleanJson(result);
            } catch (TimeoutException e) {
                log.error("AI调用超时，重试第 {} 次", i);
            } catch (Exception e) {
                log.error("AI调用失败，重试第 {} 次，错误：{}", i, e.getMessage());
            }
        }
        return null;
    }

    public Result parseFile(MultipartFile file){
        try {
            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            String fullText = "";
            // 1. 统一提取入口（兼容 PDF/Word/图片 OCR）
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
            return parseResumeText(fullText);
        }catch (Exception e) {
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
        List<Job> jobList = jobService.list();
        Map<String, Job> jobMap = jobList.stream()
                .collect(Collectors.toMap(Job::getId, job -> job));

        List<JobVo> jobVoList = jobList.stream()
                .map(job -> {
                    JobVo vo = new JobVo();
                    vo.setId(job.getId());
                    vo.setCompany(job.getCompany());
                    vo.setPosition(job.getPosition());
                    vo.setLocations(job.getLocations());
                    vo.setJdText(job.getJdText());
                    return vo;
                })
                .collect(Collectors.toList());

        List<MatchJobVo> voList = new ArrayList<>();
        String ask = null;
        try {
            ask = "这是是当前求职者的简历信息:" + resumeText + "。这是当前数据库中所有岗位的信息:" + objectMapper.writeValueAsString(jobVoList) + "。根据求职者简历匹配数据库中是否有合适的的岗位。将合适的岗位id收集起来返回(最多返回30个)。如果数据库中没有合适的岗位就返回空数组。格式如下：" +
                    "  \"jobids\": [\n" +
                    "    \"11111\",\n" +
                    "    \"22222\"\n" +
                    "  ]\n" +
                    "}";
        } catch (JsonProcessingException e) {
            log.error("匹配岗位失败!", e);
            return Result.success(voList);
        }

        try {
            String result = callWithMessage(ask,3,20000);
            List<String> jobIds = objectMapper.readValue(
                    objectMapper.readTree(result).get("jobids").toString(),
                    List.class
            );
            if (jobIds.size() == 0) {
                return Result.success(voList);
            }
            for (String jobId : jobIds) {
                Job job = jobMap.get(jobId);
                MatchJobVo vo = new MatchJobVo();
                vo.setId(job.getId());
                vo.setCompany(job.getCompany());
                vo.setPosition(job.getPosition());
                voList.add(vo);
            }
            return Result.success(voList);
        } catch (Exception e) {
            log.error("匹配岗位失败!", e);
        }
        return Result.success(voList);
    }

    public Result judgmentResume(String resumeText) {
        String ask = "这是是当前求职者上传的简历信息："+resumeText+"判断这个文本是否是一份简历。如果是简历返回true,如果不是简历或者是用户随便输入的。返回false。";
        try {
            String result = callWithMessage(ask,3,20000);
            if(result.equals("true") || result.contains("true")){
                return Result.success(result);
            }else {
                return Result.error("请输入正确的简历信息！");
            }
        } catch (Exception e) {
            return Result.error("请输入正确的简历信息！");
        }
    }

    public Result judgmentPosition(String position) {
        String ask = "这是是当前求职者想要寻找的岗位："+position+"判断这个岗位市面上是否存在。如果存在返回true,如果市面上不存在该岗位或者岗位是用户随便输入的。返回false。";
        try {
            String result = callWithMessage(ask,3,20000);
            if(result.equals("true")  || result.contains("true")){
                return Result.success(result);
            }else {
                return Result.error("岗位不存在！请输入正确的岗位信息！");
            }
        } catch (Exception e) {
            return Result.error("请输入正确的岗位名称！");
        }
    }

    public Result<PersonalAbilityImgVo> generatePersonalAbility(String resumeText) {
        String ask = "这是是当前求职者的简历信息：" + resumeText + "!根据简历信息生成一个六边形的个人能力数据（ability不是固定的。根据简历随机找出六个即可）。格式如下：" +
                "{\n" +
                "  \"personalAbility\": [\n" +
                "    {\n" +
                "      \"ability\": \"沟通能力\",\n" +
                "      \"score\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ability\": \"问题解决能力\",\n" +
                "      \"score\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ability\": \"团队合作能力\",\n" +
                "      \"score\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ability\": \"计算机能力\",\n" +
                "      \"score\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ability\": \"英文水平\",\n" +
                "      \"score\": \"100\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ability\": \"数学能力\",\n" +
                "      \"score\": \"100\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        try {
            String result = callWithMessage(ask,3,20000);
            PersonalAbilityImgVo vo = objectMapper.readValue(result,
                    new TypeReference<PersonalAbilityImgVo>() {
                    });
            if (vo == null || vo.getPersonalAbility() == null
                    || vo.getPersonalAbility().size() != 6) {
                // ✅ 返回空数组，不报错
                PersonalAbilityImgVo emptyVo = new PersonalAbilityImgVo();
                emptyVo.setPersonalAbility(Collections.emptyList());
                return Result.success(emptyVo);
            }
            return Result.success(vo);
        } catch (Exception e) {
            log.error("个人能力图生成失败", e);
            // ✅ 异常时也返回空数组，避免前端报错
            PersonalAbilityImgVo emptyVo = new PersonalAbilityImgVo();
            emptyVo.setPersonalAbility(Collections.emptyList());
            return Result.success(emptyVo);
        }
    }

    public Result<TestQuestionListVo> generateTestQuestion(String jobId, String position) {
        if (StringUtils.isEmpty(position)) {return Result.error("岗位信息不能为空！");}
        if (StringUtils.isEmpty(jobId)) {//如果是jobId是空。让ai生成测试题
            return aiGnederTestQuestion(position);
        }else {
            //分析是否是有效岗位
            LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<TestQuestion>().eq(TestQuestion::getJobId, jobId).ne(TestQuestion::getType,5);
            List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);
            // 如果是有效岗位，从数据库中查询测试题
            if (testQuestionList.size() >= 10){
                TestQuestionListVo vo = new TestQuestionListVo();
                List<TestQuestionVo> testQuestionVoList = new ArrayList<>();
                for (TestQuestion testQuestion : testQuestionList) {
                    TestQuestionVo testQuestionVo = new TestQuestionVo();
                    List<TestQuestionOptionVo> testQuestionOptionVoList = new ArrayList<>();
                    if (!StringUtils.isEmpty(testQuestion.getField1())){
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("A");
                        testQuestionOptionVo.setText(testQuestion.getField1());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    } else if (!StringUtils.isEmpty(testQuestion.getField2())) {
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("B");
                        testQuestionOptionVo.setText(testQuestion.getField2());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    }else if (!StringUtils.isEmpty(testQuestion.getField3())) {
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("C");
                        testQuestionOptionVo.setText(testQuestion.getField3());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    }else if (!StringUtils.isEmpty(testQuestion.getField4())) {
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("D");
                        testQuestionOptionVo.setText(testQuestion.getField4());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    }else if (!StringUtils.isEmpty(testQuestion.getField5())) {
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("E");
                        testQuestionOptionVo.setText(testQuestion.getField5());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    }else if (!StringUtils.isEmpty(testQuestion.getField6())) {
                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
                        testQuestionOptionVo.setOption("F");
                        testQuestionOptionVo.setText(testQuestion.getField6());
                        testQuestionOptionVoList.add(testQuestionOptionVo);
                    }
                    testQuestionVo.setName(testQuestion.getName());
                    testQuestionVo.setAnswer(testQuestion.getAnswer());
                    testQuestionVo.setType(testQuestion.getType());
                    testQuestionVo.setTitle(testQuestion.getTitle());
                    testQuestionVo.setOptions(testQuestionOptionVoList);
                    testQuestionVoList.add(testQuestionVo);
                }
                vo.setQuestions(testQuestionVoList);
                return Result.success(vo);
                // 如果是无效岗位。ai生成测试题
            }else {
                return aiGnederTestQuestion(position);
            }
        }
    }

    public Result<TestQuestionListVo> aiGnederTestQuestion(String position) {
        String ask = "这是是当前求职者想要寻找的岗位：" + position + "!帮我生成十道这个岗位专属的测试的问题。格式按照下面的来" +
                "{        \"questions\": [\n" +
                "            {\n" +
                "                \"title\": \"在开发一个基于Spring Boot的RESTful API时，以下哪种注解用于指定请求路径并确保URL映射的正确性？\",\n" +
                "                \"type\": 1,\n" +
                "                \"options\": [\n" +
                "                    {\n" +
                "                        \"option\": \"A\",\n" +
                "                        \"text\": \"@RequestMapping用于类级别定义基础路径，@GetMapping/@PostMapping用于方法级别的特定HTTP方法映射。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"B\",\n" +
                "                        \"text\": \"@Path用于替代@RequestMapping，提供更简洁的路径定义方式。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"C\",\n" +
                "                        \"text\": \"@URLMapping是Spring Boot特有的注解，专门用于REST API的路径映射。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"D\",\n" +
                "                        \"text\": \"使用@RestController注解即可自动完成所有路径映射，无需额外配置。\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"answer\": \"D\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"title\": \"在MySQL数据库中，执行大批量数据插入操作时，哪种方式能显著提升性能？\",\n" +
                "                \"type\": 1,\n" +
                "                \"options\": [\n" +
                "                    {\n" +
                "                        \"option\": \"A\",\n" +
                "                        \"text\": \"使用多条独立的INSERT INTO语句逐条插入数据。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"B\",\n" +
                "                        \"text\": \"启用事务批量提交，将数据分成若干批次，每批10000条执行一次COMMIT。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"C\",\n" +
                "                        \"text\": \"使用LOAD DATA INFILE命令直接从CSV文件高速导入数据。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"D\",\n" +
                "                        \"text\": \"关闭数据库日志功能后执行插入，完成后重新开启日志以提升速度。\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"answer\": \"B\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"title\": \"在前端开发中，Vue.js组件间通信的方式中，哪种适合实现深层嵌套组件之间的数据传递？\",\n" +
                "                \"type\": 1,\n" +
                "                \"options\": [\n" +
                "                    {\n" +
                "                        \"option\": \"A\",\n" +
                "                        \"text\": \"通过props逐层向下传递，events逐层向上触发。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"B\",\n" +
                "                        \"text\": \"使用Vuex进行全局状态管理，集中处理共享数据。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"C\",\n" +
                "                        \"text\": \"利用localStorage存储数据，各组件自行读取。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"D\",\n" +
                "                        \"text\": \"通过直接引用父组件实例this.$parent进行访问。\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"answer\": \"B\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"title\": \"在使用Docker部署全栈应用时，如何有效管理前后端服务的依赖关系和启动顺序？\",\n" +
                "                \"type\": 1,\n" +
                "                \"options\": [\n" +
                "                    {\n" +
                "                        \"option\": \"A\",\n" +
                "                        \"text\": \"手动依次启动后端容器再启动前端容器，确保依赖先行。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"B\",\n" +
                "                        \"text\": \"使用Docker Compose定义服务依赖，通过depends_on控制启动顺序。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"C\",\n" +
                "                        \"text\": \"将前后端打包进同一个镜像，避免跨服务调用问题。\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"option\": \"D\",\n" +
                "                        \"text\": \"依赖Kubernetes自动调度，无需关心启动顺序。\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"answer\": \"B\"\n" +
                "            }\n" +
                "        ]" +
                "}";
        try {
            String result = callWithMessage(ask,2,30000);
            TestQuestionListVo vo = objectMapper.readValue(result,
                    new TypeReference<TestQuestionListVo>() {
                    });
            if (vo != null) {
                List<TestQuestionVo> questions = vo.getQuestions();
                for (TestQuestionVo question : questions) {
                    question.setName(position+"模拟测试题");
                }
                return Result.success(vo);
            } else {
                return Result.error("测试问题生成失败！");
            }
        } catch (Exception e) {
            log.error("测试问题生成失败", e);
        }
        return Result.error("测试问题生成失败！");
    }


    public Result<PerformanceAnalysis> analysisResult(AnalysisResultDto dto) {
        String ask = "这是我想要的数据格式" + "{\n" +
                "  \"culturalCompatibility\" : 25(学历和岗位匹配得分，返回纯数字即可),\n" +
                "  \"resumeMatchingScore\" : 25(简历和岗位匹配得分，返回纯数字即可),\n" +
                "  \"overallPerformance\" : 36(超越了百分之多少同岗位的求职者，返回纯数字即可),\n" +
                "  \"improvements\" : [ {\n" +
                "    \"title\" : \"审计合规意识不足\",\n" +
                "    \"description\" : \"在第三题中选择“自行补录模拟附件”属于严重违反审计档案完整性和可追溯性原则的行为，暴露了对审计工作严谨性的理解偏差。应该立即上报主管，启动追溯程序，并记录缺失原因，确保审计证据链真实可靠。\"\n" +
                "  }, {\n" +
                "    \"title\" : \"缺乏独立审计实操经验\",\n" +
                "    \"description\" : \"简历中虽提及协助财务核算与流程优化，但未体现参与与独立审计项目，编制底稿或展开实质性审计程序的经验，与岗位要求的审计主导能力存在明显差距。建议通过实习或模拟项目积累真实审计流程经验。\"\n" +
                "    }\n" +
                "  ],\n" +
                "}" + "improvements为缺点以及建议(最好是三条。可以多点可以少点。2-5条最好)。这是学生的简历：" + dto.getResumeText() + "！这是学生的期望求职岗位：" + dto.getPosition()
                + "！根据简历以及期望岗位帮我生成一份类似的信息。";
        try {
            String result = callWithMessage(ask,3,20000);
            System.out.println(result);
            // 将 JSON 字符串转换为 List<MatchPositionVo>
            PerformanceAnalysis performanceAnalysis = objectMapper.readValue(result,
                    new TypeReference<PerformanceAnalysis>() {
                    });
            return Result.success(performanceAnalysis);
        } catch (Exception e) {
            log.error("解析结果失败!", e);
        }
        return Result.error("解析结果失败！");
    }

    public Result<EndResultVo> recommendationMentor(RecommendationMentorDto dto) {
        List<Mentor> mentorList = mentorService.list(new LambdaQueryWrapper<Mentor>().eq(Mentor::getMenState,3));
        if (mentorList.size() == 0) {
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
            if (StringUtils.isEmpty(endResult)) {
                return  Result.error("导师推荐失败！");
            }
            // 将 JSON 字符串转换为 List<MatchPositionVo>
            EndResultVo endResultVo = objectMapper.readValue(endResult,
                    new TypeReference<EndResultVo>() {
                    });
            return Result.success(endResultVo);
        } catch (Exception e) {
            log.error("导师推荐失败!",e);
        }
        return Result.error("导师推荐失败!");
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
}