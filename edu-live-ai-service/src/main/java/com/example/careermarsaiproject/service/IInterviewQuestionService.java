//package com.example.careermarsaiproject.service;
//
//import cn.hutool.http.HttpUtil;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONUtil;
//import com.alibaba.dashscope.aigc.generation.Generation;
//import com.alibaba.dashscope.aigc.generation.GenerationParam;
//import com.alibaba.dashscope.common.Message;
//import com.alibaba.dashscope.common.Role;
//import com.alibaba.dashscope.exception.ApiException;
//import com.alibaba.dashscope.exception.InputRequiredException;
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.example.careermarsaiproject.base.Result;
//import com.example.careermarsaiproject.config.AiConfig;
////import com.example.careermarsaiproject.dto.*;
//import com.example.careermarsaiproject.config.AiJsonCleanerConfig;
//import com.example.careermarsaiproject.config.OpenAiConfig;
//import com.example.careermarsaiproject.dto.AnalysisResultDto;
//import com.example.careermarsaiproject.dto.CharacteristicsTestReportDto;
//import com.example.careermarsaiproject.dto.RecommendationMentorDto;
//import com.example.careermarsaiproject.entity.Job;
//import com.example.careermarsaiproject.entity.Mentor;
//import com.example.careermarsaiproject.entity.TestQuestion;
//import com.example.careermarsaiproject.vo.*;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.theokanning.openai.completion.chat.ChatCompletionRequest;
//import com.theokanning.openai.completion.chat.ChatCompletionResult;
//import com.theokanning.openai.completion.chat.ChatMessage;
//import com.theokanning.openai.completion.chat.ChatMessageRole;
//import com.theokanning.openai.service.OpenAiService;
//import io.micrometer.common.util.StringUtils;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class AiAnswerService {
//    @Autowired
//    private AiConfig aiConfig;
//    @Autowired
//    private IMentorService mentorService;
//    @Autowired
//    private IJobService jobService;
//    //    @Autowired
////    private IIndustryService industryService;
////    @Autowired
////    private IInterviewQuestionService interviewQuestionService;
//    @Autowired
//    private ITestQuestionService testQuestionService;
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private OpenAiService openAiService ;
//    @Autowired
//    private OpenAiConfig openAiConfig ;
////    @Autowired
////    private IStudentService studentService;
//
//    public String callWithMessage(String question) throws ApiException, NoApiKeyException, InputRequiredException {
//        Generation gen = new Generation();
//        GenerationParam param = null;
//
//        try {
//            Message systemMsg = Message.builder()
//                    .role(Role.SYSTEM.getValue())
//                    .content("You are a helpful assistant.")
//                    .build();
//
//            Message userMsg = Message.builder()
//                    .role(Role.USER.getValue())
//                    .content(question)
//                    .build();
//            param = GenerationParam.builder()
//                    // 若没有配置环境变量，请用阿里云百炼API Key将下行替换为：.apiKey("sk-xxx")
//                    .apiKey(aiConfig.getApiKey())
//                    // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
//                    .model(aiConfig.getModel())
//                    .messages(Arrays.asList(systemMsg, userMsg))
//                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
//                    .build();
//        } catch (ApiException e) {
//            System.err.println("错误信息：" + e.getMessage());
//            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
//        }
//        if (param == null) {
//            return null;
//        }
//        return gen.call(param).getOutput().getChoices().get(0).getMessage().getContent();
//    }
//
//    public Result parseFile(MultipartFile file){
//        try {
//            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
//            String fullText = "";
//            // 1. 统一提取入口（兼容 PDF/Word/图片 OCR）
//            if (fileName.endsWith(".pdf")) {
//                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
//                    fullText = new PDFTextStripper().getText(doc);
//                }
//            } else if (fileName.endsWith(".docx")) {
//                try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
//                    StringBuilder sb = new StringBuilder();
//                    doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
//                    doc.getTables().forEach(t -> t.getRows().forEach(r -> r.getTableCells().forEach(c -> sb.append(c.getText()).append(" "))));
//                    fullText = sb.toString();
//                }
//            } else if (fileName.matches(".*\\.(jpg|jpeg|png)$")) {
//                // 保持原 parseImage 逻辑：发送 OCR 请求并拼接文本
//                String base64Img = Base64.getEncoder().encodeToString(file.getBytes());
//                String raw = HttpUtil.post("http://127.0.0.1:1224/api/ocr", JSONUtil.toJsonStr(Collections.singletonMap("base64", base64Img)));
//                JSONArray data = JSONUtil.parseObj(raw).getJSONArray("data");
//                // 模拟 parseImage 的行为，将 OCR 结果转为换行文本交由核心逻辑处理
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < data.size(); i++) sb.append(data.getJSONObject(i).getStr("text")).append("\n");
//                fullText = sb.toString();
//            }
//            if (StringUtils.isEmpty(fullText)){
//                return Result.error("文件转文本失败！");
//            }
//            return Result.success(fullText);
//        }catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return null;
//    }
//
//
//    public Result<CharacteristicsTestListVo> generateCharacteristicsTest(String resumeText) {
//        CharacteristicsTestListVo voList = new CharacteristicsTestListVo();
//        String ask = "这是是当前求职者的简历信息:" + resumeText + "先判断这个信息是不是一份简历。如果不是简历返回一个空数组即可。如果是简历。帮我根据简历生成十道性格测试题。格式如下：" +
//                "{\n" +
//                "  \"questions\": [\n" +
//                "    {\n" +
//                "      \"title\": \"你在团队中通常扮演什么角色？\",\n" +
//                "      \"options\": [\n" +
//                "        {\n" +
//                "          \"option\": \"A\",\n" +
//                "          \"text\": \"领导者，负责统筹和决策\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"B\",\n" +
//                "          \"text\": \"协调者，促进沟通与合作\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"C\",\n" +
//                "          \"text\": \"执行者，专注完成任务\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"D\",\n" +
//                "          \"text\": \"创新者，提出新想法和方案\"\n" +
//                "        }\n" +
//                "      ]\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"title\": \"面对压力时，你更倾向于？\",\n" +
//                "      \"options\": [\n" +
//                "        {\n" +
//                "          \"option\": \"A\",\n" +
//                "          \"text\": \"冷静分析问题，寻找解决方案\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"B\",\n" +
//                "          \"text\": \"寻求他人支持和建议\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"C\",\n" +
//                "          \"text\": \"暂时回避，等情绪平复后再处理\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"D\",\n" +
//                "          \"text\": \"快速行动，通过做事来缓解焦虑\"\n" +
//                "        }\n" +
//                "      ]\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"title\": \"你更喜欢的工作方式是？\",\n" +
//                "      \"options\": [\n" +
//                "        {\n" +
//                "          \"option\": \"A\",\n" +
//                "          \"text\": \"有明确流程和规范的稳定工作\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"B\",\n" +
//                "          \"text\": \"充满变化和挑战的动态工作\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"C\",\n" +
//                "          \"text\": \"可以自主安排时间和节奏的工作\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"option\": \"D\",\n" +
//                "          \"text\": \"需要与很多人互动和合作的工作\"\n" +
//                "        }\n" +
//                "      ]\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}"+ AiConfig.FORCE_PROMPT;
//        try {
//            String result = callWithMessage(ask);
////            String result2 = "{\n" +
////                    "  \"questions\" : [ {\n" +
////                    "    \"title\" : \"在团队项目中，你通常承担什么样的角色？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"组织者，负责统筹规划和任务分配\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"执行者，专注于高质量完成分配的任务\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"协调者，促进团队成员之间的沟通与协作\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"创新者，提出新思路并推动方案优化\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"当你面对一个全新的工作任务时，你的第一反应是？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"积极学习相关知识，快速掌握所需技能\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"寻求有经验的人指导，确保方向正确\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"先观察整体情况，再逐步制定行动计划\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"立即动手尝试，在实践中不断调整\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你在工作中遇到困难或挑战时，通常会？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"冷静分析问题根源，并寻找解决方案\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"主动向同事或上级求助，共同解决\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"暂时放下，等情绪稳定后再重新思考\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"尝试多种方法，灵活应对直到解决问题\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你如何看待团队合作？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"非常重视，认为团队协作是成功的关键\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"愿意配合，但更关注个人职责的完成\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"视情况而定，根据任务决定是否需要合作\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"享受合作过程，喜欢通过交流激发创意\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"当你需要在短时间内完成一项紧急任务时，你会？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"迅速制定计划，高效推进以确保按时完成\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"请求支援，确保任务质量和进度\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"专注细节，即使时间紧张也力求完美\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"边做边调整，优先保证任务顺利完成\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你如何适应一个新的工作环境？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"主动了解规则和文化，快速融入团队\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"通过观察他人行为来逐步适应\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"等待他人主动接触，慢慢建立关系\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"积极参与活动，迅速建立人际网络\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你更倾向于如何提升自己？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"系统学习专业知识和技能\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"通过实践积累经验\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"向优秀的人请教和模仿\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"参加培训和项目，拓宽视野\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你在做决策时更依赖？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"数据分析和逻辑推理\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"他人的意见和建议\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"直觉和感觉\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"过往经验和实际情况\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你如何看待工作中的责任？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"主动承担责任，追求卓越结果\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"履行分内职责，确保不出现差错\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"在被赋予责任时认真对待\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"愿意为团队目标承担额外责任\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"你如何处理多任务并行的情况？\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"制定优先级，按计划逐一完成\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"同时推进多个任务，保持灵活性\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"集中精力完成一个再开始下一个\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"根据截止时间和重要性动态调整\"\n" +
////                    "    } ]\n" +
////                    "  } ]\n" +
////                    "}";
//            voList = objectMapper.readValue(result, new TypeReference<CharacteristicsTestListVo>() {});
//            return Result.success(voList);
//        } catch (Exception e) {
//            return Result.error("测试问题生成失败！");
//        }
//    }
//
//    public Result<CharacteristicsTestReportVo> generateReport(CharacteristicsTestReportDto dto) {
//        CharacteristicsTestReportVo vo = new CharacteristicsTestReportVo();
//        String characteristicsTest = dto.getCharacteristicsTest();
//        String userAnswers = dto.getUserAnswers();
//        String resumeText = dto.getResumeText();
//        String ask = "这是性格测试题:" + characteristicsTest + "这是求职者的答题结果:" + userAnswers +"这是求职者的简历文本:" + resumeText +  "根据这些信息帮我生成性格测试报告以及推荐几个岗位（岗位名称、推荐理由和岗位匹配程度 最高分100，最低分60）。格式如下：" +
//                "{\n" +
//                "  \"testReportText\": \"根据您的性格测试结果分析，您属于‘ENTP 型 – 辩论家人格’。您富有创造力、好奇心强、喜欢挑战传统思维，擅长从多角度分析问题并提出创新性解决方案。您在高压环境下仍能保持理性思考，适合从事需要快速决策、灵活应变以及高度沟通协作的工作。\",\n" +
//                "  \"recommendedPositionList\": [\n" +
//                "    {\n" +
//                "      \"positionName\": \"金融分析师\",\n" +
//                "      \"matchDegree\": \"92\",\n" +
//                "      \"reasonsForRecommendation\": \"该岗位需要强大的逻辑思维与数据分析能力，与您理性、善于质疑和验证的性格高度契合。\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"positionName\": \"投资顾问\",\n" +
//                "      \"matchDegree\": \"88\",\n" +
//                "      \"reasonsForRecommendation\": \"投资顾问需要快速捕捉市场变化并提出创新策略，符合您喜欢挑战传统、寻找新机会的特质。\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"positionName\": \"管理咨询顾问\",\n" +
//                "      \"matchDegree\": \"85\",\n" +
//                "      \"reasonsForRecommendation\": \"咨询行业强调问题解决与跨部门沟通，您擅长从多角度分析问题并推动方案落地，非常适合该岗位。\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "      \"positionName\": \"数据分析师\",\n" +
//                "      \"matchDegree\": \"80\",\n" +
//                "      \"reasonsForRecommendation\": \"该岗位注重事实与逻辑，您在处理复杂数据时能够保持客观理性，有利于做出精准判断。\"\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}"+ AiConfig.FORCE_PROMPT;
//        try {
//            String result = callWithMessage(ask);
////            String result = "{\n" +
////                    "  \"testReportText\": \"根据您的性格测试结果分析，您属于‘ENTP 型 – 辩论家人格’。您富有创造力、好奇心强、喜欢挑战传统思维，擅长从多角度分析问题并提出创新性解决方案。您在高压环境下仍能保持理性思考，适合从事需要快速决策、灵活应变以及高度沟通协作的工作。\",\n" +
////                    "  \"recommendedPositionList\": [\n" +
////                    "    {\n" +
////                    "      \"positionName\": \"金融分析师\",\n" +
////                    "      \"matchDegree\": \"92\",\n" +
////                    "      \"reasonsForRecommendation\": \"该岗位需要强大的逻辑思维与数据分析能力，与您理性、善于质疑和验证的性格高度契合。\"\n" +
////                    "    },\n" +
////                    "    {\n" +
////                    "      \"positionName\": \"投资顾问\",\n" +
////                    "      \"matchDegree\": \"88\",\n" +
////                    "      \"reasonsForRecommendation\": \"投资顾问需要快速捕捉市场变化并提出创新策略，符合您喜欢挑战传统、寻找新机会的特质。\"\n" +
////                    "    },\n" +
////                    "    {\n" +
////                    "      \"positionName\": \"数据分析师\",\n" +
////                    "      \"matchDegree\": \"80\",\n" +
////                    "      \"reasonsForRecommendation\": \"该岗位注重事实与逻辑，您在处理复杂数据时能够保持客观理性，有利于做出精准判断。\"\n" +
////                    "    }\n" +
////                    "  ]\n" +
////                    "}";
//            vo = objectMapper.readValue(result, new TypeReference<CharacteristicsTestReportVo>() {});
//            return Result.success(vo);
//        } catch (Exception e) {
//            return Result.error("性格测试报告生成失败！");
//        }
//    }
//
//    public Result<List<MatchJobVo>> matchJobByResumeText(String resumeText) {
//        List<Job> jobList = jobService.list();
//        Map<String, Job> jobMap = jobList.stream()
//                .collect(Collectors.toMap(Job::getId, job -> job));
//        List<JobVo> jobVoList = new ArrayList<>();
//        for (Job job : jobList) {
//            JobVo vo = new JobVo();
//            vo.setId(job.getId());
//            vo.setCompany(job.getCompany());
//            vo.setPosition(job.getPosition());
//            vo.setLocations(job.getLocations());
//            vo.setJdText(job.getJdText());
//            jobVoList.add(vo);
//        }
//        List<MatchJobVo> voList = new ArrayList<>();
//        String ask = "这是是当前求职者的简历信息:" + resumeText + "。这是当前数据库中所有岗位的信息:" + jobVoList.toString() + "。根据求职者简历匹配数据库中是否有合适的的岗位。将合适的岗位id收集起来返回。如果数据库中没有合适的岗位就返回空数组。这是我需要的格式" +
//                "{\n" +
//                "  \"jobids\": [\n" +
//                "    \"11111\",\n" +
//                "    \"22222\",\n" +
//                "    \"33333\",\n" +
//                "    \"44444\",\n" +
//                "    \"55555\"\n" +
//                "  ]\n" +
//                "}";
//        try {
//            String result = callWithMessage(ask);
////            String result = "[]";
//            System.out.println(result);
//            List<String> jobIds = objectMapper.readValue(
//                    objectMapper.readTree(result).get("jobids").toString(),
//                    List.class
//            );
//            for (String jobId : jobIds) {
//                Job job = jobMap.get(jobId);
//                MatchJobVo vo = new MatchJobVo();
//                vo.setId(job.getId());
//                vo.setCompany(job.getCompany());
//                vo.setPosition(job.getPosition());
//                voList.add(vo);
//            }
//            return Result.success(voList);
//        } catch (Exception e) {
//            return Result.success(voList);
//        }
//    }
//
//    public Result judgmentPosition(String position) {
//        String ask = "这是是当前求职者想要寻找的岗位："+position+"判断这个岗位是否存在。如果存在返回true,如果市面上不存在该岗位或者岗位是用户随便输入的。返回false。只返回true或者false,不要返回任何多余的文字！";
//        try {
//            String result = callWithMessage(ask);
//            if(result.equals("true")){
//                return Result.success(result);
//            }else {
//                return Result.error("岗位不存在！请输入正确的岗位信息！");
//            }
//        } catch (Exception e) {
//            return Result.error("测试问题生成失败！");
//        }
//    }
//
//    public Result<TestQuestionListVo> generateTestQuestion(String jobId, String position) {
//        if (StringUtils.isEmpty(position)) {return Result.error("岗位信息不能为空！");}
//        if (StringUtils.isEmpty(jobId)) {//如果是jobId是空。让ai生成测试题
//            return aiGnederTestQuestion(position);
//        }else {
//            //分析是否是有效岗位
//            LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<TestQuestion>().eq(TestQuestion::getJobId, jobId).ne(TestQuestion::getType,5);
//            List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);
//            // 如果是有效岗位，从数据库中查询测试题
//            if (testQuestionList.size() >= 10){
//                TestQuestionListVo vo = new TestQuestionListVo();
//                List<TestQuestionVo> testQuestionVoList = new ArrayList<>();
//                for (TestQuestion testQuestion : testQuestionList) {
//                    TestQuestionVo testQuestionVo = new TestQuestionVo();
//                    List<TestQuestionOptionVo> testQuestionOptionVoList = new ArrayList<>();
//                    if (!StringUtils.isEmpty(testQuestion.getField1())){
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("A");
//                        testQuestionOptionVo.setText(testQuestion.getField1());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    } else if (!StringUtils.isEmpty(testQuestion.getField2())) {
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("B");
//                        testQuestionOptionVo.setText(testQuestion.getField2());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    }else if (!StringUtils.isEmpty(testQuestion.getField3())) {
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("C");
//                        testQuestionOptionVo.setText(testQuestion.getField3());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    }else if (!StringUtils.isEmpty(testQuestion.getField4())) {
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("D");
//                        testQuestionOptionVo.setText(testQuestion.getField4());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    }else if (!StringUtils.isEmpty(testQuestion.getField5())) {
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("E");
//                        testQuestionOptionVo.setText(testQuestion.getField5());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    }else if (!StringUtils.isEmpty(testQuestion.getField6())) {
//                        TestQuestionOptionVo testQuestionOptionVo = new TestQuestionOptionVo();
//                        testQuestionOptionVo.setOption("F");
//                        testQuestionOptionVo.setText(testQuestion.getField6());
//                        testQuestionOptionVoList.add(testQuestionOptionVo);
//                    }
//                    testQuestionVo.setName(testQuestion.getName());
//                    testQuestionVo.setAnswer(testQuestion.getAnswer());
//                    testQuestionVo.setType(testQuestion.getType());
//                    testQuestionVo.setTitle(testQuestion.getTitle());
//                    testQuestionVo.setOptions(testQuestionOptionVoList);
//                    testQuestionVoList.add(testQuestionVo);
//                }
//                vo.setQuestions(testQuestionVoList);
//                return Result.success(vo);
//                // 如果是无效岗位。ai生成测试题
//            }else {
//                return aiGnederTestQuestion(position);
//            }
//        }
//    }
//
////    public Result<TestQuestionListVo> aiGnederTestQuestion(String position) {
////        TestQuestionListVo vo = new TestQuestionListVo();
////        String ask = "这是是当前求职者想要寻找的岗位：" + position + "!帮我生成十道这个岗位专属的测试的问题。格式按照下面的来" +
////                "{        \"questions\": [\n" +
////                "            {\n" +
////                "                \"title\": \"在开发一个基于Spring Boot的RESTful API时，以下哪种注解用于指定请求路径并确保URL映射的正确性？\",\n" +
////                "                \"type\": 1,\n" +
////                "                \"options\": [\n" +
////                "                    {\n" +
////                "                        \"option\": \"A\",\n" +
////                "                        \"text\": \"@RequestMapping用于类级别定义基础路径，@GetMapping/@PostMapping用于方法级别的特定HTTP方法映射。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"B\",\n" +
////                "                        \"text\": \"@Path用于替代@RequestMapping，提供更简洁的路径定义方式。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"C\",\n" +
////                "                        \"text\": \"@URLMapping是Spring Boot特有的注解，专门用于REST API的路径映射。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"D\",\n" +
////                "                        \"text\": \"使用@RestController注解即可自动完成所有路径映射，无需额外配置。\"\n" +
////                "                    }\n" +
////                "                ],\n" +
////                "                \"answer\": \"D\"\n" +
////                "            },\n" +
////                "            {\n" +
////                "                \"title\": \"在MySQL数据库中，执行大批量数据插入操作时，哪种方式能显著提升性能？\",\n" +
////                "                \"type\": 1,\n" +
////                "                \"options\": [\n" +
////                "                    {\n" +
////                "                        \"option\": \"A\",\n" +
////                "                        \"text\": \"使用多条独立的INSERT INTO语句逐条插入数据。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"B\",\n" +
////                "                        \"text\": \"启用事务批量提交，将数据分成若干批次，每批1000条执行一次COMMIT。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"C\",\n" +
////                "                        \"text\": \"使用LOAD DATA INFILE命令直接从CSV文件高速导入数据。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"D\",\n" +
////                "                        \"text\": \"关闭数据库日志功能后执行插入，完成后重新开启日志以提升速度。\"\n" +
////                "                    }\n" +
////                "                ],\n" +
////                "                \"answer\": \"B\"\n" +
////                "            },\n" +
////                "            {\n" +
////                "                \"title\": \"在前端开发中，Vue.js组件间通信的方式中，哪种适合实现深层嵌套组件之间的数据传递？\",\n" +
////                "                \"type\": 1,\n" +
////                "                \"options\": [\n" +
////                "                    {\n" +
////                "                        \"option\": \"A\",\n" +
////                "                        \"text\": \"通过props逐层向下传递，events逐层向上触发。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"B\",\n" +
////                "                        \"text\": \"使用Vuex进行全局状态管理，集中处理共享数据。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"C\",\n" +
////                "                        \"text\": \"利用localStorage存储数据，各组件自行读取。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"D\",\n" +
////                "                        \"text\": \"通过直接引用父组件实例this.$parent进行访问。\"\n" +
////                "                    }\n" +
////                "                ],\n" +
////                "                \"answer\": \"B\"\n" +
////                "            },\n" +
////                "            {\n" +
////                "                \"title\": \"在使用Docker部署全栈应用时，如何有效管理前后端服务的依赖关系和启动顺序？\",\n" +
////                "                \"type\": 1,\n" +
////                "                \"options\": [\n" +
////                "                    {\n" +
////                "                        \"option\": \"A\",\n" +
////                "                        \"text\": \"手动依次启动后端容器再启动前端容器，确保依赖先行。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"B\",\n" +
////                "                        \"text\": \"使用Docker Compose定义服务依赖，通过depends_on控制启动顺序。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"C\",\n" +
////                "                        \"text\": \"将前后端打包进同一个镜像，避免跨服务调用问题。\"\n" +
////                "                    },\n" +
////                "                    {\n" +
////                "                        \"option\": \"D\",\n" +
////                "                        \"text\": \"依赖Kubernetes自动调度，无需关心启动顺序。\"\n" +
////                "                    }\n" +
////                "                ],\n" +
////                "                \"answer\": \"B\"\n" +
////                "            }\n" +
////                "        ]" +
////                "}"+ AiConfig.FORCE_PROMPT;
////        try {
//////            String result = callWithMessage(ask);
////
////            String result = "{\n" +
////                    "    \"questions\": [\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Java开发中，以下哪个关键字用于实现类的继承，从而支持面向对象编程中的多态特性？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"implements\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"extends\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"inherits\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"super\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"B\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Spring框架中，@Autowired注解的主要作用是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"用于自动将Bean注册到Spring容器中。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"用于标记一个方法为初始化回调方法。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"用于自动装配Bean，由Spring容器注入依赖对象。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"用于启用组件扫描以发现所有@Service注解的类。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"C\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Java集合框架中，HashMap和TreeMap的主要区别是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"HashMap基于哈希表实现，不保证顺序；TreeMap基于红黑树实现，按键自然排序或自定义排序。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"HashMap线程安全，TreeMap不是线程安全的。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"HashMap不允许null键和null值，TreeMap允许。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"HashMap性能低于TreeMap，因为后者使用了更高效的算法。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"A\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在使用MyBatis进行数据库操作时，#{}和${}的区别是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"#{}用于预编译传参，防止SQL注入；${}用于字符串拼接，存在SQL注入风险。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"${}用于参数类型转换，#{}用于原始值直接插入。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"#{}只能用于数值类型，${}可用于字符串类型。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"#{}和${}功能完全相同，只是语法风格不同。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"A\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Java中，以下哪段代码能够正确创建一个线程并启动执行？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"new Thread().run();\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"new Thread(() -> System.out.println(\\\"Hello\\\")).start();\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"Thread t = new Thread(); t.run();\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"Runnable r = () -> start(); r.run();\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"B\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Spring Boot应用中，application.properties文件的作用是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"定义Maven依赖版本管理。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"配置应用程序运行时的参数，如端口、数据库连接等。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"声明Spring Bean的注解扫描路径。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"编写SQL脚本用于数据库初始化。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"B\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Java中，String、StringBuilder和StringBuffer之间的主要区别是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"String是可变的，StringBuilder和StringBuffer是不可变的。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"String和StringBuilder是线程安全的，StringBuffer不是。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"String是不可变对象；StringBuilder非线程安全但效率高；StringBuffer线程安全但性能较低。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"三者没有本质区别，可以随意替换使用。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"C\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Spring MVC中，@RequestMapping注解的method属性用于指定什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"控制器方法返回的数据类型。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"HTTP请求的URL路径。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"允许处理的HTTP请求方法类型，如GET、POST等。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"请求参数的绑定方式。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"C\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在Java异常处理机制中，try-catch-finally语句块中finally的作用是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"仅在catch捕获异常后执行。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"无论是否发生异常都会执行的清理代码，常用于资源释放。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"用于抛出新的异常。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"替代try块中的业务逻辑执行。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"B\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"title\": \"在使用JPA进行数据持久化时，@Entity注解的作用是什么？\",\n" +
////                    "            \"type\": 1,\n" +
////                    "            \"options\": [\n" +
////                    "                {\n" +
////                    "                    \"option\": \"A\",\n" +
////                    "                    \"text\": \"标识该类是一个Spring Service组件。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"B\",\n" +
////                    "                    \"text\": \"表示该类的方法将被事务管理。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"C\",\n" +
////                    "                    \"text\": \"标记该类为一个持久化实体类，对应数据库中的表。\"\n" +
////                    "                },\n" +
////                    "                {\n" +
////                    "                    \"option\": \"D\",\n" +
////                    "                    \"text\": \"启用Hibernate二级缓存功能。\"\n" +
////                    "                }\n" +
////                    "            ],\n" +
////                    "            \"answer\": \"C\"\n" +
////                    "        }\n" +
////                    "    ]\n" +
////                    "}";
//////            String result = "{\n" +
//////                    "  \"questions\" : [ {\n" +
//////                    "    \"title\" : \"在开发一个基于Spring Boot的RESTful API时，以下哪种注解用于指定请求路径并确保URL映射的正确性？\",\n" +
//////                    "    \"answer\" : \"D\",\n" +
//////                    "    \"type\" : \"1\",\n" +
//////                    "    \"options\" : [ {\n" +
//////                    "      \"option\" : \"A\",\n" +
//////                    "      \"text\" : \"@RequestMapping用于类级别定义基础路径，@GetMapping/@PostMapping用于方法级别的特定HTTP方法映射。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"B\",\n" +
//////                    "      \"text\" : \"@Path用于替代@RequestMapping，提供更简洁的路径定义方式。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"C\",\n" +
//////                    "      \"text\" : \"@URLMapping是Spring Boot特有的注解，专门用于REST API的路径映射。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"D\",\n" +
//////                    "      \"text\" : \"使用@RestController注解即可自动完成所有路径映射，无需额外配置。\"\n" +
//////                    "    } ]\n" +
//////                    "  }, {\n" +
//////                    "    \"title\" : \"在MySQL数据库中，执行大批量数据插入操作时，哪种方式能显著提升性能？\",\n" +
//////                    "    \"answer\" : \"B\",\n" +
//////                    "    \"type\" : \"1\",\n" +
//////                    "    \"options\" : [ {\n" +
//////                    "      \"option\" : \"A\",\n" +
//////                    "      \"text\" : \"使用多条独立的INSERT INTO语句逐条插入数据。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"B\",\n" +
//////                    "      \"text\" : \"启用事务批量提交，将数据分成若干批次，每批1000条执行一次COMMIT。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"C\",\n" +
//////                    "      \"text\" : \"使用LOAD DATA INFILE命令直接从CSV文件高速导入数据。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"D\",\n" +
//////                    "      \"text\" : \"关闭数据库日志功能后执行插入，完成后重新开启日志以提升速度。\"\n" +
//////                    "    } ]\n" +
//////                    "  }, {\n" +
//////                    "    \"title\" : \"在前端开发中，Vue.js组件间通信的方式中，哪种适合实现深层嵌套组件之间的数据传递？\",\n" +
//////                    "    \"answer\" : \"B\",\n" +
//////                    "    \"type\" : \"1\",\n" +
//////                    "    \"options\" : [ {\n" +
//////                    "      \"option\" : \"A\",\n" +
//////                    "      \"text\" : \"通过props逐层向下传递，events逐层向上触发。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"B\",\n" +
//////                    "      \"text\" : \"使用Vuex进行全局状态管理，集中处理共享数据。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"C\",\n" +
//////                    "      \"text\" : \"利用localStorage存储数据，各组件自行读取。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"D\",\n" +
//////                    "      \"text\" : \"通过直接引用父组件实例this.$parent进行访问。\"\n" +
//////                    "    } ]\n" +
//////                    "  }, {\n" +
//////                    "    \"title\" : \"在Node.js中处理高并发请求时，以下哪种策略最有助于提升应用性能和稳定性？\",\n" +
//////                    "    \"answer\" : \"D\",\n" +
//////                    "    \"type\" : \"1\",\n" +
//////                    "    \"options\" : [ {\n" +
//////                    "      \"option\" : \"A\",\n" +
//////                    "      \"text\" : \"使用同步代码确保每个请求按顺序执行，避免竞争条件。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"B\",\n" +
//////                    "      \"text\" : \"采用Cluster模块启动多个进程，充分利用多核CPU资源。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"C\",\n" +
//////                    "      \"text\" : \"增加单个进程内存限制至4GB以上以支持更多请求。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"D\",\n" +
//////                    "      \"text\" : \"禁用垃圾回收机制以减少运行时停顿时间。\"\n" +
//////                    "    } ]\n" +
//////                    "  }, {\n" +
//////                    "    \"title\" : \"在使用Docker部署全栈应用时，如何有效管理前后端服务的依赖关系和启动顺序？\",\n" +
//////                    "    \"answer\" : \"B\",\n" +
//////                    "    \"type\" : \"1\",\n" +
//////                    "    \"options\" : [ {\n" +
//////                    "      \"option\" : \"A\",\n" +
//////                    "      \"text\" : \"手动依次启动后端容器再启动前端容器，确保依赖先行。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"B\",\n" +
//////                    "      \"text\" : \"使用Docker Compose定义服务依赖，通过depends_on控制启动顺序。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"C\",\n" +
//////                    "      \"text\" : \"将前后端打包进同一个镜像，避免跨服务调用问题。\"\n" +
//////                    "    }, {\n" +
//////                    "      \"option\" : \"D\",\n" +
//////                    "      \"text\" : \"依赖Kubernetes自动调度，无需关心启动顺序。\"\n" +
//////                    "    } ]\n" +
//////                    "  } ]\n" +
//////                    "}";
////            // 将 JSON 字符串转换为 List<MatchPositionVo>
////            Optional<String> jsonOpt = AiJsonCleanerConfig.extractJson(result);
////
////            if (jsonOpt.isEmpty()) {
////                return Result.error("AI 返回格式异常，请重试");
////            }
////
//    ////            TestQuestionListVo vo = objectMapper.readValue(
//    ////                    jsonOpt.get(),
//    ////                    new TypeReference<TestQuestionListVo>() {}
//    ////            );
////            vo = objectMapper.readValue(jsonOpt.get(),
////                    new TypeReference<TestQuestionListVo>() {
////                    });
////            if (vo != null) {
////                List<TestQuestionVo> questions = vo.getQuestions();
////                for (TestQuestionVo question : questions) {
////                    question.setName(position+"模拟测试题");
////                }
////                return Result.success(vo);
////            } else {
////                return Result.error("测试问题生成失败！");
////            }
////        } catch (Exception e) {
////            return Result.error("测试问题生成失败！");
////        }
////    }
//
//    public Result<TestQuestionListVo> aiGnederTestQuestion(String position) {
//        // 1. 优化 Prompt：增加格式约束
//        String ask = "你是一位资深的 HR 和技术专家。请为岗位 [" + position + "] 生成10道模拟面试题。\n" +
//                "要求：\n" +
//                "1. 必须严格遵守以下 JSON 格式：\n" +
//                "{\"questions\": [{\"title\":\"题目\",\"type\":1,\"options\":[{\"option\":\"A\",\"text\":\"选项内容\"}],\"answer\":\"A\"}]}\n" +
//                "2. 禁止输出任何解释性文字、开场白或 Markdown 代码块标记（如 ```json）。\n" +
//                "3. 确保 JSON 对象完整，不要遗漏任何键名（Key）。";
//
//        try {
//            // 2. 调用 AI
//            String result = callWithMessage(ask);
//
//            // 3. 使用增强型清洗器提取 JSON
//            Optional<String> jsonOpt = AiJsonCleanerConfig.extractJson(result);
//
//            if (jsonOpt.isEmpty()) {
//                // 这里可以记录日志，方便排查是哪个模型输出了不可修复的脏数据
//                return Result.error("AI 生成的题目格式解析失败，请点击重试。");
//            }
//
//            // 4. 使用配置好的容错 Mapper 进行反序列化
//            TestQuestionListVo vo = objectMapper.readValue(
//                    jsonOpt.get(),
//                    new TypeReference<TestQuestionListVo>() {}
//            );
//
//            if (vo != null && vo.getQuestions() != null) {
//                for (TestQuestionVo question : vo.getQuestions()) {
//                    question.setName(position + " 模拟测试题");
//                }
//                return Result.success(vo);
//            } else {
//                return Result.error("生成的题目列表为空。");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error("处理异常: " + e.getMessage());
//        }
//    }
//
//
//    public Result<PerformanceAnalysis> analysisResult(AnalysisResultDto dto) {
//        PerformanceAnalysis performanceAnalysis = new PerformanceAnalysis();
//        String ask = "这是我想要的数据格式" + "{\n" +
//                "  \"culturalCompatibility\" : 25(学历和岗位匹配得分，返回纯数字即可),\n" +
//                "  \"resumeMatchingScore\" : 25(简历和岗位匹配得分，返回纯数字即可),\n" +
//                "  \"overallPerformance\" : 36(超越了百分之多少同岗位的求职者，返回纯数字即可),\n" +
//                "  \"improvements\" : [ {\n" +
//                "    \"title\" : \"审计合规意识不足\",\n" +
//                "    \"description\" : \"在第三题中选择“自行补录模拟附件”属于严重违反审计档案完整性和可追溯性原则的行为，暴露了对审计工作严谨性的理解偏差。应该立即上报主管，启动追溯程序，并记录缺失原因，确保审计证据链真实可靠。\"\n" +
//                "  }, {\n" +
//                "    \"title\" : \"缺乏独立审计实操经验\",\n" +
//                "    \"description\" : \"简历中虽提及协助财务核算与流程优化，但未体现参与与独立审计项目，编制底稿或展开实质性审计程序的经验，与岗位要求的审计主导能力存在明显差距。建议通过实习或模拟项目积累真实审计流程经验。\"\n" +
//                "    }\n" +
//                "  ],\n" +
//                "}" + "improvements为缺点以及建议(最好是三条。可以多点可以少点。2-5条最好)。这是学生的简历：" + dto.getResumeText() + "！这是学生的期望求职岗位：" + dto.getPosition()
//                + "！根据简历以及期望岗位帮我生成一份类似的信息。"+ AiConfig.FORCE_PROMPT;
//
//        try {
//            String result = callWithMessage(ask);
////            String result = "{\n" +
////                    "  \"avgScore\": 40,\n" +
////                    "  \"resumeMatchingScore\": 30,\n" +
////                    "  \"positionMatchingScore\": 35,\n" +
////                    "  \"overallPerformance\": 27,\n" +
////                    "  \"improvements\": [\n" +
////                    "    {\n" +
////                    "      \"title\": \"项目深度描述缺失\",\n" +
////                    "      \"description\": \"简历未详细说明在阿里巴巴和腾讯的具体项目成果与技术挑战，建议补充使用Java/前端技术解决的实际业务问题案例。\"\n" +
////                    "    },\n" +
////                    "    {\n" +
////                    "      \"title\": \"技能量化不足\",\n" +
////                    "      \"description\": \"Java开发与前端开发技能未标注掌握程度（如SSM框架、React等）及项目应用规模，建议量化描述（如'基于Spring Boot完成日均百万级API开发'）。\"\n" +
////                    "    }\n" +
////                    "  ]\n" +
////                    "}";
//            System.out.println(result);
//
//            // 将 JSON 字符串转换为 List<MatchPositionVo>
//            performanceAnalysis = objectMapper.readValue(result,
//                    new TypeReference<PerformanceAnalysis>() {
//                    });
//            System.out.println(performanceAnalysis);
//            return Result.success(performanceAnalysis);
//        } catch (Exception e) {
//            return Result.error("解析结果失败！");
//        }
//    }
//
//    public Result<EndResultVo> recommendationMentor(RecommendationMentorDto dto) {
//        EndResultVo endResultVo = new EndResultVo();
//        List<MentorVo> voList = new ArrayList<>();
//        List<Mentor> mentorList = mentorService.list(new LambdaQueryWrapper<Mentor>().eq(Mentor::getMenState,3));
//        for (Mentor mentor : mentorList) {
//            MentorVo vo = new MentorVo();
//            BeanUtils.copyProperties(mentor, vo);
//            voList.add(vo);
//        }
////        dto.setResume("name: \"张三\",\n" +
////                "          educationalQualifications: \"本科\",\n" +
////                "          skill: \"Java开发，前端开发\",\n" +
////                "          educationalExperience: \"计算机科学与技术专业，本科\",\n" +
////                "          jobExperience: \"拥有三年开发经验。入职过两家公司，分别为阿里巴巴和腾讯。\"");
////        dto.setExpectationPosition("java开发工程师");
//        String ask = "这是学生的期望求职岗位：" + dto.getPosition() + "。这是所有导师的数据:" + voList + "帮我匹配合适的导师，将适合的导师id收集起来发我.我只要id，逗号隔开。推荐的导师最多十个"+ AiConfig.FORCE_PROMPT;
//        try {
//            String result = callWithMessage(ask);
////            String result = "[]";
//            if (StringUtils.isEmpty(result) || result.equals("[]")) {
//                return  Result.error("导师推荐失败！");
//            }
//            System.out.println(result);
//            List<String> idList = Arrays.stream(result.split(","))
//                    .collect(Collectors.toList());
//            if (idList.size() == 0) {
//                return  Result.error("导师推荐失败！");
//            }
//            LambdaQueryWrapper<Mentor> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.in(Mentor::getId, idList);
//            List<Mentor> endMentorList = mentorService.list(queryWrapper);
//            if (endMentorList.size() == 0) {
//                return  Result.error("导师推荐失败！");
//            }
//            String endAsk = "这是学生的期望求职岗位：" + dto.getPosition() + "。这是匹配的导师的数据:" + endMentorList + "帮我生成这样的对象返回，marryRate匹配率，successRate为成功功率，rating为导师评分，reasons为推荐理由，这几个参数帮我动态生成，其他从数据中拿就好，以下是我需要的格式" +
//                    "{\n" +
//                    "  \"mentorList\": [\n" +
//                    "    {\n" +
//                    "      \"id\": 导师id,\n" +
//                    "      \"marryRate\": 90,\n" +
//                    "      \"menName\": \"David Chen\",\n" +
//                    "      \"lableNames\": \"Ex-Google PM Career Coach | Behavioral lnterview Specialist\",\n" +
//                    "      \"rating\": 4.5,\n" +
//                    "      \"studyCount\": 95,\n" +
//                    "      \"successRate\": 95,\n" +
//                    "      \"reasons\": [\n" +
//                    "        \"精通审计合规与风险报告机制，可针对性解决审计意识不足问题\",\n" +
//                    "        \"具备集团及审计制度设计经验，与目标岗位高度匹配\",\n" +
//                    "        \"擅长系统性思维训练，能强化对审计闭环管理的理解\"\n" +
//                    "      ]\n" +
//                    "    },\n" +
//                    "    {\n" +
//                    "      \"id\": 导师id,\n" +
//                    "      \"marryRate\": 85,\n" +
//                    "      \"menName\": \"Emily Wang\",\n" +
//                    "      \"lableNames\": \"Former McKinsey Consultant | Case Interview Expert\",\n" +
//                    "      \"rating\": 4.2,\n" +
//                    "      \"studyCount\": 78,\n" +
//                    "      \"successRate\": 88,\n" +
//                    "      \"reasons\": [\n" +
//                    "        \"拥有顶级咨询公司背景，深谙案例面试套路\",\n" +
//                    "        \"擅长结构化思维训练，快速提升解题能力\",\n" +
//                    "        \"熟悉各行业商业模型，提供实战性建议\"\n" +
//                    "      ]\n" +
//                    "    }\n" +
//                    "  ]\n" +
//                    "}"+ AiConfig.FORCE_PROMPT;
//            String endResult = callWithMessage(endAsk);
//            if (StringUtils.isEmpty(endResult) || result.equals("[]")) {
//                return  Result.error("导师推荐失败！");
//            }
//            // 将 JSON 字符串转换为 List<MatchPositionVo>
//            endResultVo = objectMapper.readValue(endResult,
//                    new TypeReference<EndResultVo>() {
//                    });
//            return Result.success(endResultVo);
//        } catch (Exception e) {
//            return Result.error("导师推荐失败");
//        }
//    }
//
//    public String callWithMessage2(String question) {
//
//        ChatMessage systemMsg = new ChatMessage(
//                ChatMessageRole.SYSTEM.value(),
//                "You are a helpful assistant."
//        );
//
//        ChatMessage userMsg = new ChatMessage(
//                ChatMessageRole.USER.value(),
//                question
//        );
//
//        ChatCompletionRequest request = ChatCompletionRequest.builder()
//                .model(openAiConfig.getModel())
//                .messages(Arrays.asList(systemMsg, userMsg))
//                .temperature(0.7)
//                .build();
//
//        ChatCompletionResult result =
//                openAiService.createChatCompletion(request);
//
//        return result.getChoices()
//                .get(0)
//                .getMessage()
//                .getContent();
//    }
//}