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
//import com.example.careermarsaiproject.dto.*;
//import com.example.careermarsaiproject.dto.Industry;
//import com.example.careermarsaiproject.dto.Student;
//import com.example.careermarsaiproject.dto.TestQuestion;
//import com.example.careermarsaiproject.entity.CourseComment;
//import com.example.careermarsaiproject.entity.Job;
//import com.example.careermarsaiproject.entity.Mentor;
//import com.example.careermarsaiproject.utils.IdWorker;
//import com.example.careermarsaiproject.vo.*;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.poi.xwpf.usermodel.*;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Service
//public class AiService {
//    @Autowired
//    private AiConfig aiConfig;
//    @Autowired
//    private IMentorService mentorService;
//    @Autowired
//    private IJobService jobService;
//    @Autowired
//    private IIndustryService industryService;
//    @Autowired
//    private IInterviewQuestionService interviewQuestionService;
//    @Autowired
//    private ITestQuestionService testQuestionService;
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private IStudentService studentService;
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
//
//    public List<Mentor> selectMentorList() {
//        List<CommentVo> voList = new ArrayList<>();
//        List<Mentor> mentorList = mentorService.list();
//        List<String> idList = mentorList.stream().map(Mentor::getId).toList();
//        String ask = "帮我生成" + mentorList.size() + "条评论。大体内容是学生给老师的正面的评价，比如课程内容或者直播内容的点评。" +
//                "这是导师的所有id" + idList + "下面是我需要的格式，不要生成多余的文字，我要把内容转成java对象的。严格按照我需要的格式来！" +
//                "[ {\n" +
//                "  \"id\" : \"1718601222713196545\",\n" +
//                "  \"comment\" : \"老师讲解非常清晰，知识点环环相扣，收获很大。\"\n" +
//                "}, {\n" +
//                "  \"id\" : \"1740642005964161026\",\n" +
//                "  \"comment\" : \"课程内容实用，案例贴近实际，容易理解。\"\n" +
//                "}, {\n" +
//                "  \"id\" : \"1740657497634373634\",\n" +
//                "  \"comment\" : \"知识点讲解透彻，不留疑问。\"\n" +
//                "} ]";
//        try {
//            String result = callWithMessage(ask);
//            voList = objectMapper.readValue(result,
//                    new TypeReference<List<CommentVo>>() {
//                    });
//
//            List<CourseComment> courseCommentList = new ArrayList<>();
//            for (CommentVo vo : voList) {
//                CourseComment courseComment = new CourseComment();
//                courseComment.setId(IdWorker.getId().toString());
//                courseComment.setMentorId(vo.getId());
//                courseComment.setCommentDetails(vo.getComment());
//                courseComment.setCommentDate(LocalDateTime.now());
//                courseCommentList.add(courseComment);
//            }
////            courseCommentService.saveBatch(courseCommentList);
//            System.out.println(result);
//        } catch (Exception e) {
//            return null;
//        }
//        return mentorService.list();
//    }
//
//
//    public List<Job> searchAllJob() {
//        return jobService.list();
//    }
//
//    public List<MatchPositionVo> matchPosition(MatchPositionDto dto) {
//        List<Job> jobList = jobService.list();
//        List<MatchPositionVo> voList = new ArrayList<>();
//        String jobString = jobList.toString();
//        System.out.println(jobString);
//        String ask = "这是是当前求职者的信息:" + dto.toString() + "。这是jobList,当前所有岗位的信息:" + jobList.toString() + "。根据求职者的信息帮我分析该求职者能从事哪些岗位的工作。将可以从事的工作封装成一个list集合    @ApiModelProperty(value = \"公司名称\")\n" +
//                "    private String company;\n" +
//                "    @ApiModelProperty(value = \"岗位名称\")\n" +
//                "    private String position;这是对应的参数。匹配的岗位只从jobList中找，没有合适的就返回空数组。只返回公司名称和岗位名称即可。不要返回任何多余的文字。我要需要把数据转成java对象的.如果没有合适的岗位，就返回一个[]的空数组";
//        try {
//            String result = callWithMessage(ask);
////            String result = "[]";
////            String result = " [\n" +
////                    "        {\n" +
////                    "            \"company\": \"阿里巴巴\",\n" +
////                    "            \"position\": \"Java开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"腾讯\",\n" +
////                    "            \"position\": \"后端开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"字节跳动\",\n" +
////                    "            \"position\": \"全栈开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"百度\",\n" +
////                    "            \"position\": \"前端开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"华为\",\n" +
////                    "            \"position\": \"软件开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"京东\",\n" +
////                    "            \"position\": \"Java后端工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"美团\",\n" +
////                    "            \"position\": \"Web前端开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"拼多多\",\n" +
////                    "            \"position\": \"中级Java开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"网易\",\n" +
////                    "            \"position\": \"前端开发工程师\"\n" +
////                    "        },\n" +
////                    "        {\n" +
////                    "            \"company\": \"小米\",\n" +
////                    "            \"position\": \"软件工程师（Java方向）\"\n" +
////                    "        }\n" +
////                    "    ]";
//            voList = objectMapper.readValue(result,
//                    new TypeReference<List<MatchPositionVo>>() {
//                    });
//        } catch (Exception e) {
//            return null;
//        }
//        return voList;
//    }
//
//    public Result<TestQuestionListVo> generateQuestion(String position) {
//        TestQuestionListVo vo = new TestQuestionListVo();
//        String ask = "这是是当前求职者想要寻找的岗位：" + position + "!帮我生成一些这个岗位专属的测试的问题。生成的问题最少三个，最多十个。不要生成多余的任何文字。直接返回结果。我要将你回答的结果直接转换成对象的。如果这个岗位是求职者瞎写的。返回一个[]空的数组就行。这是我需要的格式。注意标点符号还有字段名严格按照我定义的来" +
//                "{        \"questions\": [\n" +
//                "            {\n" +
//                "                \"title\": \"在开发一个基于Spring Boot的RESTful API时，以下哪种注解用于指定请求路径并确保URL映射的正确性？\",\n" +
//                "                \"type\": 1,\n" +
//                "                \"options\": [\n" +
//                "                    {\n" +
//                "                        \"option\": \"A\",\n" +
//                "                        \"text\": \"@RequestMapping用于类级别定义基础路径，@GetMapping/@PostMapping用于方法级别的特定HTTP方法映射。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"B\",\n" +
//                "                        \"text\": \"@Path用于替代@RequestMapping，提供更简洁的路径定义方式。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"C\",\n" +
//                "                        \"text\": \"@URLMapping是Spring Boot特有的注解，专门用于REST API的路径映射。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"D\",\n" +
//                "                        \"text\": \"使用@RestController注解即可自动完成所有路径映射，无需额外配置。\"\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"answer\": \"D\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"title\": \"在MySQL数据库中，执行大批量数据插入操作时，哪种方式能显著提升性能？\",\n" +
//                "                \"type\": 1,\n" +
//                "                \"options\": [\n" +
//                "                    {\n" +
//                "                        \"option\": \"A\",\n" +
//                "                        \"text\": \"使用多条独立的INSERT INTO语句逐条插入数据。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"B\",\n" +
//                "                        \"text\": \"启用事务批量提交，将数据分成若干批次，每批1000条执行一次COMMIT。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"C\",\n" +
//                "                        \"text\": \"使用LOAD DATA INFILE命令直接从CSV文件高速导入数据。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"D\",\n" +
//                "                        \"text\": \"关闭数据库日志功能后执行插入，完成后重新开启日志以提升速度。\"\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"answer\": \"B\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"title\": \"在前端开发中，Vue.js组件间通信的方式中，哪种适合实现深层嵌套组件之间的数据传递？\",\n" +
//                "                \"type\": 1,\n" +
//                "                \"options\": [\n" +
//                "                    {\n" +
//                "                        \"option\": \"A\",\n" +
//                "                        \"text\": \"通过props逐层向下传递，events逐层向上触发。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"B\",\n" +
//                "                        \"text\": \"使用Vuex进行全局状态管理，集中处理共享数据。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"C\",\n" +
//                "                        \"text\": \"利用localStorage存储数据，各组件自行读取。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"D\",\n" +
//                "                        \"text\": \"通过直接引用父组件实例this.$parent进行访问。\"\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"answer\": \"B\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"title\": \"在Node.js中处理高并发请求时，以下哪种策略最有助于提升应用性能和稳定性？\",\n" +
//                "                \"type\": 1,\n" +
//                "                \"options\": [\n" +
//                "                    {\n" +
//                "                        \"option\": \"A\",\n" +
//                "                        \"text\": \"使用同步代码确保每个请求按顺序执行，避免竞争条件。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"B\",\n" +
//                "                        \"text\": \"采用Cluster模块启动多个进程，充分利用多核CPU资源。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"C\",\n" +
//                "                        \"text\": \"增加单个进程内存限制至4GB以上以支持更多请求。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"D\",\n" +
//                "                        \"text\": \"禁用垃圾回收机制以减少运行时停顿时间。\"\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"answer\": \"D\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"title\": \"在使用Docker部署全栈应用时，如何有效管理前后端服务的依赖关系和启动顺序？\",\n" +
//                "                \"type\": 1,\n" +
//                "                \"options\": [\n" +
//                "                    {\n" +
//                "                        \"option\": \"A\",\n" +
//                "                        \"text\": \"手动依次启动后端容器再启动前端容器，确保依赖先行。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"B\",\n" +
//                "                        \"text\": \"使用Docker Compose定义服务依赖，通过depends_on控制启动顺序。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"C\",\n" +
//                "                        \"text\": \"将前后端打包进同一个镜像，避免跨服务调用问题。\"\n" +
//                "                    },\n" +
//                "                    {\n" +
//                "                        \"option\": \"D\",\n" +
//                "                        \"text\": \"依赖Kubernetes自动调度，无需关心启动顺序。\"\n" +
//                "                    }\n" +
//                "                ],\n" +
//                "                \"answer\": \"B\"\n" +
//                "            }\n" +
//                "        ]" +
//                "}";
//        try {
//            String result = callWithMessage(ask);
////            String result = "{\n" +
////                    "  \"questions\" : [ {\n" +
////                    "    \"title\" : \"在开发一个基于Spring Boot的RESTful API时，以下哪种注解用于指定请求路径并确保URL映射的正确性？\",\n" +
////                    "    \"answer\" : \"D\",\n" +
////                    "    \"type\" : \"1\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"@RequestMapping用于类级别定义基础路径，@GetMapping/@PostMapping用于方法级别的特定HTTP方法映射。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"@Path用于替代@RequestMapping，提供更简洁的路径定义方式。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"@URLMapping是Spring Boot特有的注解，专门用于REST API的路径映射。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"使用@RestController注解即可自动完成所有路径映射，无需额外配置。\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"在MySQL数据库中，执行大批量数据插入操作时，哪种方式能显著提升性能？\",\n" +
////                    "    \"answer\" : \"B\",\n" +
////                    "    \"type\" : \"1\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"使用多条独立的INSERT INTO语句逐条插入数据。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"启用事务批量提交，将数据分成若干批次，每批1000条执行一次COMMIT。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"使用LOAD DATA INFILE命令直接从CSV文件高速导入数据。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"关闭数据库日志功能后执行插入，完成后重新开启日志以提升速度。\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"在前端开发中，Vue.js组件间通信的方式中，哪种适合实现深层嵌套组件之间的数据传递？\",\n" +
////                    "    \"answer\" : \"B\",\n" +
////                    "    \"type\" : \"1\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"通过props逐层向下传递，events逐层向上触发。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"使用Vuex进行全局状态管理，集中处理共享数据。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"利用localStorage存储数据，各组件自行读取。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"通过直接引用父组件实例this.$parent进行访问。\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"在Node.js中处理高并发请求时，以下哪种策略最有助于提升应用性能和稳定性？\",\n" +
////                    "    \"answer\" : \"D\",\n" +
////                    "    \"type\" : \"1\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"使用同步代码确保每个请求按顺序执行，避免竞争条件。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"采用Cluster模块启动多个进程，充分利用多核CPU资源。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"增加单个进程内存限制至4GB以上以支持更多请求。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"禁用垃圾回收机制以减少运行时停顿时间。\"\n" +
////                    "    } ]\n" +
////                    "  }, {\n" +
////                    "    \"title\" : \"在使用Docker部署全栈应用时，如何有效管理前后端服务的依赖关系和启动顺序？\",\n" +
////                    "    \"answer\" : \"B\",\n" +
////                    "    \"type\" : \"1\",\n" +
////                    "    \"options\" : [ {\n" +
////                    "      \"option\" : \"A\",\n" +
////                    "      \"text\" : \"手动依次启动后端容器再启动前端容器，确保依赖先行。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"B\",\n" +
////                    "      \"text\" : \"使用Docker Compose定义服务依赖，通过depends_on控制启动顺序。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"C\",\n" +
////                    "      \"text\" : \"将前后端打包进同一个镜像，避免跨服务调用问题。\"\n" +
////                    "    }, {\n" +
////                    "      \"option\" : \"D\",\n" +
////                    "      \"text\" : \"依赖Kubernetes自动调度，无需关心启动顺序。\"\n" +
////                    "    } ]\n" +
////                    "  } ]\n" +
////                    "}";
//            // 将 JSON 字符串转换为 List<MatchPositionVo>
//            vo = objectMapper.readValue(result,
//                    new TypeReference<TestQuestionListVo>() {
//                    });
//            if (vo != null) {
//                return Result.success(vo);
//            } else {
//                return Result.error("测试问题生成失败！");
//            }
//        } catch (Exception e) {
//            return Result.error("测试问题生成失败！");
//        }
//    }
//
//    public PerformanceAnalysis analysisResult(AnalysisResultDto dto) {
//        PerformanceAnalysis performanceAnalysis = new PerformanceAnalysis();
////        dto.setResume("name: \"张三\",\n" +
////                "          educationalQualifications: \"本科\",\n" +
////                "          skill: \"Java开发，前端开发\",\n" +
////                "          educationalExperience: \"计算机科学与技术专业，本科\",\n" +
////                "          jobExperience: \"拥有三年开发经验。入职过两家公司，分别为阿里巴巴和腾讯。\"");
////        dto.setExpectationPosition("java开发工程师");
////        dto.setInterviewScore("40");
////        String ask ="这是学生的简历："+dto.getResume()+"。这是学生的期望求职岗位："+dto.getExpectationPosition()+"。这是学生的面试得分："+dto.getInterviewScore()
////                +"。这是所有导师的数据:"+voList+"根据这些数据帮我我想要的数据信息。注意格式要跟我写的的一模一样（符号和字段不要改变）以及不要生成多余的任何文字描述。你接下来的回答我是要转成java对象的。如果没有合适的或者分析失败返回“[]”这样的空数组就行。teachers中的很多字段表里都有，直接拿就好，没有的话就帮我生成。以下是我需要的返回信息" +
////                "{\n" +
////                "  \"avgScore\" : 50,\n" +
////                "  \"resumeMatchingScore\" : 25,\n" +
////                "  \"positionMatchingScore\" : 40,\n" +
////                "  \"overallPerformance\" : 36,\n" +
////                "  \"improvements\" : [ {\n" +
////                "    \"title\" : \"审计合规意识不足\",\n" +
////                "    \"description\" : \"在第三题中选择“自行补录模拟附件”属于严重违反审计档案完整性和可追溯性原则的行为，暴露了对审计工作严谨性的理解偏差。应该立即上报主管，启动追溯程序，并记录缺失原因，确保审计证据链真实可靠。\"\n" +
////                "  }, {\n" +
////                "    \"title\" : \"缺乏独立审计实操经验\",\n" +
////                "    \"description\" : \"简历中虽提及协助财务核算与流程优化，但未体现参与与独立审计项目，编制底稿或展开实质性审计程序的经验，与岗位要求的审计主导能力存在明显差距。建议通过实习或模拟项目积累真实审计流程经验。\"\n" +
////                "  } ],\n" +
////                "  \"mentorList\" : [ {\n" +
////                "    \"marryRate\" : 90,\n" +
////                "    \"menName\" : \"David Chen\",\n" +
////                "    \"lableNames\" : \"Ex-Google PM Career Coach | Behavioral lnterview Specialist\",\n" +
////                "    \"rating\" : 4.5,\n" +
////                "    \"studyCount\" : 95,\n" +
////                "    \"successRate\" : 95,\n" +
////                "    \"reasons\" : [ \"精通审计合规与风险报告机制，可针对性解决审计意识不足问题\", \"具备集团及审计制度设计经验，与目标岗位高度匹配\", \"擅长系统性思维训练，能强化对审计闭环管理的理解\" ]\n" +
////                "  }, {\n" +
////                "    \"marryRate\" : 85,\n" +
////                "    \"menName\" : \"Emily Wang\",\n" +
////                "    \"lableNames\" : \"Former McKinsey Consultant | Case Interview Expert\",\n" +
////                "    \"rating\" : 4.2,\n" +
////                "    \"studyCount\" : 78,\n" +
////                "    \"successRate\" : 88,\n" +
////                "    \"reasons\" : [ \"拥有顶级咨询公司背景，深谙案例面试套路\", \"擅长结构化思维训练，快速提升解题能力\", \"熟悉各行业商业模型，提供实战性建议\" ]\n" +
////                "  }, {\n" +
////                "    \"marryRate\" : 88,\n" +
////                "    \"menName\" : \"Michael Zhang\",\n" +
////                "    \"lableNames\" : \"Wall Street Quant Analyst | Technical Interview Coach\",\n" +
////                "    \"rating\" : 4.7,\n" +
////                "    \"studyCount\" : 120,\n" +
////                "    \"successRate\" : 92,\n" +
////                "    \"reasons\" : [ \"顶尖投行背景，精通技术面算法\", \"独创解题方法论，快速提升编码能力\", \"熟悉各大公司面试题库，针对性辅导\" ]\n" +
////                "  } ]\n" +
////                "}";
//        String ask = "这是我想要的数据格式" + "{\n" +
//                "  \"resumeMatchingScore\" : 25,\n" +
//                "  \"overallPerformance\" : 36,\n" +
//                "  \"improvements\" : [ {\n" +
//                "    \"title\" : \"审计合规意识不足\",\n" +
//                "    \"description\" : \"在第三题中选择“自行补录模拟附件”属于严重违反审计档案完整性和可追溯性原则的行为，暴露了对审计工作严谨性的理解偏差。应该立即上报主管，启动追溯程序，并记录缺失原因，确保审计证据链真实可靠。\"\n" +
//                "  }, {\n" +
//                "    \"title\" : \"缺乏独立审计实操经验\",\n" +
//                "    \"description\" : \"简历中虽提及协助财务核算与流程优化，但未体现参与与独立审计项目，编制底稿或展开实质性审计程序的经验，与岗位要求的审计主导能力存在明显差距。建议通过实习或模拟项目积累真实审计流程经验。\"\n" +
//                "    }\n" +
//                "  ],\n" +
//                "}" + "resumeMatchingScore为简历匹配分，overallPerformance为在所有候选人中的水平，大概超越了百分之多少该岗位的求职者。improvements为缺点以及建议(最好是三条。可以多点可以少点。2-5条最好)。这是学生的简历：" + dto.getResume() + "！这是学生的期望求职岗位：" + dto.getExpectationPosition()
//                + "！根据简历以及期望岗位帮我生成一份类似的信息。注意格式要跟我要求的一模一样，不要生成多余的任何文字描述。你接下来的回答我是要转成java对象的。";
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
//        } catch (Exception e) {
//            return null;
//        }
//        return performanceAnalysis;
//    }
//
//    public EndResultVo recommendationMentor(RecommendationMentorDto dto) {
//        EndResultVo endResultVo = new EndResultVo();
//        List<MentorVo> voList = new ArrayList<>();
//        List<Mentor> mentorList = mentorService.list();
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
//        String ask = "这是学生的期望求职岗位：" + dto.getExpectationPosition() + "。这是所有导师的数据:" + voList + "帮我匹配合适的导师，将适合的导师id收集起来发我.不要生成多余的任何内容，我只要id，逗号隔开";
//        try {
//            String result = callWithMessage(ask);
////            String result = "{\n" +
////                    "  \"mentorList\": [\n" +
////                    "    {\n" +
////                    "      \"marryRate\": 90,\n" +
////                    "      \"menName\": \"David Chen\",\n" +
////                    "      \"lableNames\": \"Ex-Google PM Career Coach | Behavioral lnterview Specialist\",\n" +
////                    "      \"rating\": 4.5,\n" +
////                    "      \"studyCount\": 95,\n" +
////                    "      \"successRate\": 95,\n" +
////                    "      \"reasons\": [\n" +
////                    "        \"精通审计合规与风险报告机制，可针对性解决审计意识不足问题\",\n" +
////                    "        \"具备集团及审计制度设计经验，与目标岗位高度匹配\",\n" +
////                    "        \"擅长系统性思维训练，能强化对审计闭环管理的理解\"\n" +
////                    "      ]\n" +
////                    "    },\n" +
////                    "    {\n" +
////                    "      \"marryRate\": 85,\n" +
////                    "      \"menName\": \"Emily Wang\",\n" +
////                    "      \"lableNames\": \"Former McKinsey Consultant | Case Interview Expert\",\n" +
////                    "      \"rating\": 4.2,\n" +
////                    "      \"studyCount\": 78,\n" +
////                    "      \"successRate\": 88,\n" +
////                    "      \"reasons\": [\n" +
////                    "        \"拥有顶级咨询公司背景，深谙案例面试套路\",\n" +
////                    "        \"擅长结构化思维训练，快速提升解题能力\",\n" +
////                    "        \"熟悉各行业商业模型，提供实战性建议\"\n" +
////                    "      ]\n" +
////                    "    },\n" +
////                    "    {\n" +
////                    "      \"marryRate\": 88,\n" +
////                    "      \"menName\": \"Michael Zhang\",\n" +
////                    "      \"lableNames\": \"Wall Street Quant Analyst | Technical Interview Coach\",\n" +
////                    "      \"rating\": 4.7,\n" +
////                    "      \"studyCount\": 120,\n" +
////                    "      \"successRate\": 92,\n" +
////                    "      \"reasons\": [\n" +
////                    "        \"顶尖投行背景，精通技术面算法\",\n" +
////                    "        \"独创解题方法论，快速提升编码能力\",\n" +
////                    "        \"熟悉各大公司面试题库，针对性辅导\"\n" +
////                    "      ]\n" +
////                    "    }\n" +
////                    "  ]\n" +
////                    "}";
//            System.out.println(result);
//            List<String> idList = Arrays.stream(result.split(","))
//                    .collect(Collectors.toList());
//            LambdaQueryWrapper<Mentor> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.in(Mentor::getId, idList);
//            List<Mentor> endMentorList = mentorService.list(queryWrapper);
//            String endAsk = "这是学生的期望求职岗位：" + dto.getExpectationPosition() + "。这是匹配的导师的数据:" + endMentorList + "帮我生成这样的对象返回，marryRate匹配率，successRate为成功功率，rating为导师评分，reasons为推荐理由，这几个参数帮我动态生成，其他从数据中拿就好，不要生成任何多余的内容，我是要把你返回的内容直接转成java对象的。以下是我需要的格式" +
//                    "{\n" +
//                    "  \"mentorList\": [\n" +
//                    "    {\n" +
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
//                    "}";
//            String endResult = callWithMessage(endAsk);
//            // 将 JSON 字符串转换为 List<MatchPositionVo>
//            endResultVo = objectMapper.readValue(endResult,
//                    new TypeReference<EndResultVo>() {
//                    });
//            System.out.println(endResultVo);
//        } catch (Exception e) {
//            return null;
//        }
//        return endResultVo;
//    }
//
//    public Result<List<IndustryVo>> searchAllIndustry() {
//        LambdaQueryWrapper<Industry> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Industry::getStatus, 1);
//        List<Industry> industryList = industryService.list(queryWrapper);
//
//        List<String> industryIds = jobService.list(new LambdaQueryWrapper<Job>()
//                        .eq(Job::getStatus, 1))
//                .stream().map(Job::getIndustryId).toList();
//
//
//        List<IndustryVo> voList = new ArrayList<>();
//        for (Industry industry : industryList) {
//            if (!industryIds.contains(industry.getId())) {
//                continue;
//            }
//            IndustryVo vo = new IndustryVo();
//            vo.setIndustryId(industry.getId());
//            vo.setIndustry(industry.getIndustryName());
//            voList.add(vo);
//        }
//        return Result.success(voList);
//    }
//
//    public Result<List<JobVo>> searchJobByIndustryId(String industryId) {
//        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Job::getIndustryId, industryId);
//        queryWrapper.eq(Job::getStatus, 1);
//
//        List<Job> jobList = jobService.list(queryWrapper);
//        if (jobList.size() == 0) {
//            return null;
//        }
//        List<JobVo> voList = new ArrayList<>();
//        for (Job job : jobList) {
//            JobVo vo = new JobVo();
//            vo.setJobId(job.getId());
//            vo.setCompany(job.getCompany());
//            vo.setPosition(job.getPosition());
//            voList.add(vo);
//        }
//        return Result.success(voList);
//    }
//
//    public Result<List<TestPaperVo>> searchTestPaperByjobId(String jobId) {
//        LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TestQuestion::getJobId, jobId);
//
//        List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);
//        if (testQuestionList.size() == 0) {
//            return null;
//        }
//        // 按名称分组计数
//        Map<String, Long> nameCountMap = testQuestionList.stream()
//                .collect(Collectors.groupingBy(TestQuestion::getName, Collectors.counting()));
//
//        // 过滤出数量 >= 3 的名称，并转成 TestPaperVo
//        List<TestPaperVo> voList = nameCountMap.entrySet().stream()
//                .filter(entry -> entry.getValue() >= 3)
//                .map(entry -> {
//                    TestPaperVo vo = new TestPaperVo();
//                    vo.setName(entry.getKey());
//                    return vo;
//                })
//                .collect(Collectors.toList());
//        return Result.success(voList);
//    }
//
//    public Result<List<TestQuestionVo>> searchTestQuestionByTestPaperName(String testPaperName, String jobId) {
//        LambdaQueryWrapper<TestQuestion> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TestQuestion::getJobId, jobId);
//        queryWrapper.eq(TestQuestion::getName, testPaperName);
//
//        List<TestQuestion> testQuestionList = testQuestionService.list(queryWrapper);
//        if (testQuestionList.size() == 0) {
//            return null;
//        }
//
//        // ✅ 超过 30 道，随机抽取 30 道
//        if (testQuestionList.size() > 30) {
//            Random random = new Random();
//            testQuestionList = testQuestionList.stream()
//                    .sorted((a, b) -> random.nextInt(3) - 1) // 打乱顺序
//                    .limit(30)
//                    .collect(Collectors.toList());
//        }
//
//        List<TestQuestionVo> voList = new ArrayList<>();
//        for (TestQuestion testQuestion : testQuestionList) {
//            TestQuestionVo vo = new TestQuestionVo();
//            vo.setName(testQuestion.getName());
//            vo.setTitle(testQuestion.getTitle());
//            vo.setType(testQuestion.getType());
//            vo.setAnswer(testQuestion.getAnswer());
//            List<TestQuestionOptionVo> optionVoList = new ArrayList<>();
//
//            if (testQuestion.getType() == 1) {//如果是单选题
//                for (int i = 0; i < 4; i++) {
//                    TestQuestionOptionVo optionVo = new TestQuestionOptionVo();
//                    if (i == 0) {
//                        optionVo.setOption("A");
//                        optionVo.setText(testQuestion.getField1());
//                        optionVoList.add(optionVo);
//                    } else if (i == 1) {
//                        optionVo.setOption("B");
//                        optionVo.setText(testQuestion.getField2());
//                        optionVoList.add(optionVo);
//                    } else if (i == 2) {
//                        optionVo.setOption("C");
//                        optionVo.setText(testQuestion.getField3());
//                        optionVoList.add(optionVo);
//                    } else if (i == 3) {
//                        optionVo.setOption("D");
//                        optionVo.setText(testQuestion.getField4());
//                        optionVoList.add(optionVo);
//                    }
//                }
//                vo.setOptions(optionVoList);
//            } else if (testQuestion.getType() == 2) {//如果是多选题)
//                for (int i = 0; i < 6; i++) {
//                    TestQuestionOptionVo optionVo = new TestQuestionOptionVo();
//                    if (i == 0) {
//                        optionVo.setOption("A");
//                        optionVo.setText(testQuestion.getField1());
//                        optionVoList.add(optionVo);
//                    } else if (i == 1) {
//                        optionVo.setOption("B");
//                        optionVo.setText(testQuestion.getField2());
//                        optionVoList.add(optionVo);
//                    } else if (i == 2) {
//                        optionVo.setOption("C");
//                        optionVo.setText(testQuestion.getField3());
//                        optionVoList.add(optionVo);
//                    } else if (i == 3) {
//                        optionVo.setOption("D");
//                        optionVo.setText(testQuestion.getField4());
//                        optionVoList.add(optionVo);
//                    } else if (i == 4) {
//                        optionVo.setOption("E");
//                        optionVo.setText(testQuestion.getField4());
//                        optionVoList.add(optionVo);
//                    } else if (i == 5) {
//                        optionVo.setOption("F");
//                        optionVo.setText(testQuestion.getField4());
//                        optionVoList.add(optionVo);
//                    }
//                }
//                vo.setOptions(optionVoList);
//            } else if (testQuestion.getType() == 3) {//如果是判断题)
//                for (int i = 0; i < 2; i++) {
//                    TestQuestionOptionVo optionVo = new TestQuestionOptionVo();
//                    if (i == 0) {
//                        optionVo.setOption("A");
//                        optionVo.setText(testQuestion.getField1());
//                        optionVoList.add(optionVo);
//                    } else if (i == 1) {
//                        optionVo.setOption("B");
//                        optionVo.setText(testQuestion.getField2());
//                        optionVoList.add(optionVo);
//                    }
//                }
//                vo.setOptions(optionVoList);
//            } else if (testQuestion.getType() == 4) {//如果是填空题)
//                TestQuestionOptionVo optionVo = new TestQuestionOptionVo();
//                optionVo.setOption("A");
//                optionVo.setText(testQuestion.getField1());
//                optionVoList.add(optionVo);
//            } else if (testQuestion.getType() == 5) {//如果是性格测试题)
//                for (int i = 0; i < 4; i++) {
//                    TestQuestionOptionVo optionVo = new TestQuestionOptionVo();
//                    if (i == 0) {
//                        optionVo.setOption("A");
//                        optionVo.setText(testQuestion.getField1());
//                        optionVoList.add(optionVo);
//                    } else if (i == 1) {
//                        optionVo.setOption("B");
//                        optionVo.setText(testQuestion.getField2());
//                        optionVoList.add(optionVo);
//                    } else if (i == 2) {
//                        optionVo.setOption("C");
//                        optionVo.setText(testQuestion.getField3());
//                        optionVoList.add(optionVo);
//                    } else if (i == 3) {
//                        optionVo.setOption("D");
//                        optionVo.setText(testQuestion.getField4());
//                        optionVoList.add(optionVo);
//                    }
//                }
//                vo.setOptions(optionVoList);
//            }
//            voList.add(vo);
//        }
//        return Result.success(voList);
//    }
//
//    public Student queryStudentByPhone(String phone, String countryNum) {
//        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Student::getStuMobile, phone);
//        queryWrapper.eq(Student::getCountryNum, countryNum);
//        Student student = studentService.getOne(queryWrapper);
//        return student;
//    }
//
//    public Result<ResumeVo> parseResume(MultipartFile file) {
//        try {
//            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
//            String fullText = "";
//
//            // 1. 统一提取入口（兼容 PDF/Word/图片 OCR）
//            if (fileName.endsWith(".pdf")) {
//                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
//                    fullText = new PDFTextStripper().getText(doc);
//                }
//            } else if (fileName.endsWith(".docx")) {
//                fullText = extractDocxText(file);
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
//
//            // 2. 核心逻辑执行（完全保留您认可的逻辑 A、B、C、D）
//            String cleanedFullText = fullText.replace("：", ":").replace("｜", "|").replace("\t", " ").replace("\r", "");
//            String[] lines = cleanedFullText.split("\n");
//
//            ResumeVo vo = new ResumeVo();
//            StringBuilder skill = new StringBuilder(), job = new StringBuilder(), edu = new StringBuilder();
//            String currentBlock = "";
//            int lineIndex = 0;
//
//            for (String line : lines) {
//                lineIndex++;
//                String trimmed = line.trim();
//                if (trimmed.isEmpty()) continue;
//
//                // --- 逻辑 A: 姓名提取 ---
//                if (vo.getName() == null && lineIndex <= 10) {
//                    String flatLine = trimmed.replace(" ", "");
//                    if (flatLine.contains("姓名") || flatLine.toLowerCase().contains("name")) {
//                        String[] parts = trimmed.split("[:：]");
//                        if (parts.length > 1) vo.setName(cleanPotentialName(parts[1]));
//                    }
//                    if (vo.getName() == null) vo.setName(cleanPotentialName(trimmed));
//                    if (vo.getName() != null) continue;
//                }
//
//                // --- 逻辑 B: 杂质过滤 ---
//                if (trimmed.contains("@") || trimmed.matches(".*\\d{11}.*") || trimmed.toLowerCase().contains("http"))
//                    continue;
//
//                // --- 逻辑 C: 模糊标题路由 ---
//                String upperLine = trimmed.toUpperCase().replace(" ", "");
//                boolean isHeader = true;
//                if (upperLine.matches(".*(教育|学校|学习经历|EDUCATION|ACADEMIC|专业|主修|GPA|绩点).*"))
//                    currentBlock = "EDU";
//                else if (upperLine.matches(".*(经历|项目|EXPERIENCE|WORK|INTERN|实习|商赛|社团|工作).*"))
//                    currentBlock = "JOB";
//                else if (upperLine.matches(".*(技能|特长|SKILL|能力|证书|优势|SUMMARY|荣誉|获奖).*"))
//                    currentBlock = "SKILL";
//                else isHeader = false;
//
//                if (isHeader) continue;
//
//                // --- 逻辑 D: 内容归仓 ---
//                if ("SKILL".equals(currentBlock)) skill.append(trimmed).append(" ");
//                else if ("JOB".equals(currentBlock)) job.append(trimmed).append(" ");
//                else if ("EDU".equals(currentBlock)) edu.append(trimmed).append(" ");
//            }
//
//            // 3. 结果清洗与判定
//            vo.setSkill(cleanText(skill.toString()));
//            vo.setJobExperience(cleanText(job.toString()));
//            vo.setEducationalExperience(cleanText(edu.toString()));
//            vo.setEducationalQualifications(determineQualification(cleanedFullText + " " + fileName));
//            if (vo.getName() == null) vo.setName(extractNameFromFileName(fileName));
//
//            return Result.success(vo);
//        } catch (Exception e) {
//            return Result.error("解析失败: " + e.getMessage());
//        }
//    }
//
//    private String cleanPotentialName(String raw) {
//        if (raw == null || raw.isEmpty() || raw.contains("大学") || raw.contains("学院") || raw.contains("项目"))
//            return null;
//        String res = raw.trim();
//        for (String f : NAME_INLINE_FILTERS) {
//            int i = res.indexOf(f);
//            if (i != -1) res = res.substring(0, i);
//        }
//        res = res.replace(" ", "").replace("\t", "").replaceAll("[:：]", "");
//        Matcher m = Pattern.compile("^[\\u4E00-\\u9FA5]{2,4}").matcher(res);
//        if (m.find()) {
//            String name = m.group();
//            if (!NAME_BLACKLIST.contains(name) && COMMON_SURNAMES.contains(name.substring(0, 1))) return name;
//        }
//        return null;
//    }
//
//    private String determineQualification(String text) {
//        if (text == null || text.isEmpty()) return "未提取到";
//        if (text.contains("博士") || text.contains("PhD")) return "博士";
//        if (text.contains("硕士") || text.contains("研究生")) return "硕士";
//        if (text.contains("本科") || text.contains("学士") || text.contains("大四") || text.contains("保研") || text.contains("大学在读") || text.contains("985") || text.contains("211"))
//            return "本科";
//        return (text.contains("大专") || text.contains("专科")) ? "专科" : "未知";
//    }
//
//    private String extractNameFromFileName(String fileName) {
//        Matcher m = Pattern.compile("^[\\u4E00-\\u9FA5]{2,3}").matcher(fileName);
//        return m.find() ? m.group() : null;
//    }
//
//    private String extractDocxText(MultipartFile file) throws Exception {
//        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
//            StringBuilder sb = new StringBuilder();
//            doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
//            doc.getTables().forEach(t -> t.getRows().forEach(r -> r.getTableCells().forEach(c -> sb.append(c.getText()).append(" "))));
//            return sb.toString();
//        }
//    }
//
//    private String cleanText(String input) {
//        return (input == null || input.isEmpty()) ? "" : input.replaceAll("\\s+", " ").replaceAll("[，；;,]{2,}", "，").trim();
//    }
//
//    // 常量池（保持原样，仅补充了您提到的“初”和“曾”姓）
//    private static final Set<String> NAME_BLACKLIST = new HashSet<>(Arrays.asList("个人简历", "简历", "求职简历", "我的简历", "姓名", "性别", "联系方式", "出生年月"));
//    private static final String[] NAME_INLINE_FILTERS = {"性别", "性 别", "专业", "专 业", "学历", "联系", "电话", "Email", "(", "（", "|", "｜"};
//    private static final Set<String> COMMON_SURNAMES = new HashSet<>(Arrays.asList(
//            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦", "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳", "史", "唐", "费", "薛", "雷", "贺", "罗", "毕", "郝", "常", "傅", "卞", "齐", "康", "伍", "余", "黄", "萧", "尹", "初", "曾"
//    ));
//
//
//    public Result<ResumeVo> parseFile(MultipartFile file){
//        try {
//            String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
//            String fullText = "";
//
//            // 1. 统一提取入口（兼容 PDF/Word/图片 OCR）
//            if (fileName.endsWith(".pdf")) {
//                try (PDDocument doc = PDDocument.load(file.getInputStream())) {
//                    fullText = new PDFTextStripper().getText(doc);
//                }
//            } else if (fileName.endsWith(".docx")) {
//                fullText = extractDocxText(file);
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
//
//            String ask = "提取简历中的信息。返回我需要的数据。这是我需要的格式{\n" +
//                    "name\":  \"求职者的姓名\",\n" +
//                    "educationalQualifications\":  \"求职者的学历（只返回以下其中之一）：博士、硕士、本科、专科、其他\",\n" +
//                    "skill\":  \"求职者掌握的技能\",\n" +
//                    "educationalExperience\":  \"求职者的教育经历\"，\n" +
//                    "jobExperience\":  \"求职者的工作经历\"\n" +
//                    "}" +
//                    "不要返回任何多余的文字。我要需要把数据转成java对象的。这是是当前求职者的简历信息:" + fullText;
//            String result = callWithMessage(ask);
//            ResumeVo vo = objectMapper.readValue(result, new TypeReference<ResumeVo>() {
//            });
//            return Result.success(vo);
//        }catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return null;
//    }
//
//}