package com.example.careermarsaiproject.controller;

import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.dto.AnalysisResultDto;
import com.example.careermarsaiproject.dto.CharacteristicsTestReportDto;
import com.example.careermarsaiproject.dto.RecommendationMentorDto;
import com.example.careermarsaiproject.dto.ResumeTextDto;
import com.example.careermarsaiproject.service.AiAnswerService;
import com.example.careermarsaiproject.vo.*;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/ai")
public class AiController {
//    @Autowired
//    private AiService aiService;
    @Autowired
    private AiAnswerService aiAnswerService;

//    @ResponseBody
//    @GetMapping("ask/question")
//    public Result<String> askAi(@RequestParam("question") String question) throws Exception {
//        String result = aiService.callWithMessage(question);
//        return Result.success(result);
//    }
//
//    @ApiOperation("搜索全部导师")
//    @ResponseBody
//    @GetMapping("search/mentor")
//    public Result<List<Mentor>> searchAllMentor() throws Exception {
//        List<Mentor> mentorList = aiService.selectMentorList();
//        System.out.println(mentorList);
//        return Result.success(mentorList);
//    }
//
//    @ApiOperation("匹配工作岗位")
//    @ResponseBody
//    @PostMapping("/match/position")
//    public Result<List<MatchPositionVo>> matchPosition(@RequestBody MatchPositionDto dto){
//        List<MatchPositionVo> result = aiService.matchPosition(dto);
//        if (result != null){
//            return Result.success(result);
//        }{
//            return Result.error("匹配岗位失败！请稍后再试！");
//        }
//    }
//
//    @ApiOperation("生成测试问题")
//    @ResponseBody
//    @GetMapping("/generate/testQuestion")
//    public Result<TestQuestionListVo> generateQuestion(String position){
//        return aiService.generateQuestion(position);
//    }
//
//    @ApiOperation("搜索全部岗位")
//    @ResponseBody
//    @GetMapping("search/job")
//    public Result<List<Job>> searchAllJob() throws Exception {
//        List<Job> jobList = aiService.searchAllJob();
//        return Result.success(jobList);
//    }
//
//    @ApiOperation("分析结果")
//    @ResponseBody
//    @PostMapping("/analysis/result")
//    public Result<PerformanceAnalysis> analysisResult(@RequestBody AnalysisResultDto dto){
//        PerformanceAnalysis result = aiService.analysisResult(dto);
//        if (result != null){
//            return Result.success(result);
//        }{
//            return Result.error("分析结果失败！请稍后再试！");
//        }
//    }
//
//    @ApiOperation("分析结果")
//    @ResponseBody
//    @PostMapping("/recommendation/mentor")
//    public Result<EndResultVo> recommendationMentor(@RequestBody RecommendationMentorDto dto){
//        EndResultVo result = aiService.recommendationMentor(dto);
//        if (result != null){
//            return Result.success(result);
//        }{
//            return Result.error("分析结果失败！请稍后再试！");
//        }
//    }
//
//    @ApiOperation("解析简历图片")
//    @ResponseBody
//    @PostMapping("/parse/image")
//    public Result<ResumeVo> parseImage(@RequestParam("file") MultipartFile file) {
//        try {
//            // 验证文件
//            if (file.isEmpty()) {
//                return Result.error("请选择要上传的文件!");
//            }
//            // 验证文件类型
//            String contentType = file.getContentType();
//            if (contentType == null || !contentType.startsWith("image/")) {
//                return Result.error("请上传有效的图片!");
//            }
//            return aiService.parseFile(file);
//        } catch (Exception e) {
//            return Result.error("解析图片时发生错误: " + e.getMessage());
//        }
//    }
//
//    @ApiOperation("解析简历文件")
//    @ResponseBody
//    @PostMapping("/parse/pdf")
//    public Result<ResumeVo> parsePdf(@RequestParam("file") MultipartFile file) {
//        try {
//            if (file.isEmpty()) return Result.error("请选择要上传的文件!");
//
//            // 验证文件类型
//            String fileName = file.getOriginalFilename().toLowerCase();
//            if (!(fileName.endsWith(".pdf"))) {
//                return Result.error("只能上传pdf类型的文件!");
//            }
//            // 执行解析
//            return aiService.parseFile(file);
//        } catch (Exception e) {
//            return Result.error("解析文件失败: " + e.getMessage());
//        }
//    }
//
//    @ApiOperation("解析简历文件")
//    @ResponseBody
//    @PostMapping("/parse/docx")
//    public Result<ResumeVo> parseDocx(@RequestParam("file") MultipartFile file) {
//        try {
//            if (file.isEmpty()) return Result.error("请选择要上传的文件!");
//
//            // 验证文件类型
//            String fileName = file.getOriginalFilename().toLowerCase();
//            if (!(fileName.endsWith(".docx"))) {
//                return Result.error("只能上传docx类型的文件!");
//            }
//            // 执行解析
////            return aiService.parseResume(file);
//            return aiService.parseFile(file);
//        } catch (Exception e) {
//            return Result.error("解析文件失败: " + e.getMessage());
//        }
//    }
//
//    @ApiOperation("搜索全部行业")
//    @ResponseBody
//    @GetMapping("/search/industry")
//    public Result<List<IndustryVo>> searchAllIndustry() throws Exception {
//        return aiService.searchAllIndustry();
//    }
//
//    @ApiOperation("搜索行业下的岗位")
//    @ResponseBody
//    @GetMapping("search/job/{industryId}")
//    public Result<List<JobVo>> searchJobByIndustryId(@PathVariable String industryId) throws Exception {
//        return aiService.searchJobByIndustryId(industryId);
//    }
//
//    @ApiOperation("搜索岗位下的试卷")
//    @ResponseBody
//    @GetMapping("search/testPaper/{jobId}")
//    public Result<List<TestPaperVo>> searchTestPaperByjobId(@PathVariable String jobId) throws Exception {
//        return aiService.searchTestPaperByjobId(jobId);
//    }
//
//    @ApiOperation("搜索试卷下的笔试题")
//    @ResponseBody
//    @GetMapping("/search/testQuestion")
//    public Result<List<TestQuestionVo>> searchTestQuestionByTestPaperName(@RequestParam String testPaperName,@RequestParam String jobId) throws Exception {
//        return aiService.searchTestQuestionByTestPaperName(testPaperName,jobId);
//    }

    @ApiOperation("解析简历图片")
    @ResponseBody
    @PostMapping("/parse/image")
    public Result<ResumeVo> parseImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片!");
            }
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("请上传有效的图片!");
            }
            return aiAnswerService.parseFile(file);
        } catch (Exception e) {
            return Result.error("解析图片时发生错误: " + e.getMessage());
        }
    }

    @ApiOperation("解析pdf文件")
    @ResponseBody
    @PostMapping("/parse/pdf")
    public Result<ResumeVo> parsePdf(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return Result.error("请选择要上传的文件!");

            // 验证文件类型
            String fileName = file.getOriginalFilename().toLowerCase();
            if (!(fileName.endsWith(".pdf"))) {
                return Result.error("只能上传pdf类型的文件!");
            }
            // 执行解析
            return aiAnswerService.parseFile(file);
        } catch (Exception e) {
            return Result.error("解析文件失败: " + e.getMessage());
        }
    }

    @ApiOperation("解析docx文件")
    @ResponseBody
    @PostMapping("/parse/docx")
    public Result<ResumeVo> parseDocx(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return Result.error("请选择要上传的文件!");

            // 验证文件类型
            String fileName = file.getOriginalFilename().toLowerCase();
            if (!(fileName.endsWith(".docx"))) {
                return Result.error("只能上传docx类型的文件!");
            }
            // 执行解析
//            return aiService.parseResume(file);
            return aiAnswerService.parseFile(file);
        } catch (Exception e) {
            return Result.error("解析文件失败: " + e.getMessage());
        }
    }

    @ApiOperation("生成性格测试题")
    @ResponseBody
    @PostMapping("/generate/characteristicsTest")
    public Result<CharacteristicsTestListVo> generateCharacteristicsTest(@RequestBody String resumeText) {
        return aiAnswerService.generateCharacteristicsTest(resumeText);
    }

    @ApiOperation("生成测试报告")
    @ResponseBody
    @PostMapping("/generate/report")
    public Result<CharacteristicsTestReportVo> generateReport(@RequestBody CharacteristicsTestReportDto dto) {
        return aiAnswerService.generateReport(dto);
    }

    @ApiOperation("根据简历文本匹配岗位")
    @ResponseBody
    @PostMapping("/match/job")
    public Result<List<MatchJobVo>> matchJobByResumeText(@RequestBody String resumeText) {
        return aiAnswerService.matchJobByResumeText(resumeText);
    }

    @ApiOperation("判断是否是简历")
    @ResponseBody
    @PostMapping("/judgment/resume")
    public Result judgmentResume(@RequestBody String resumeText){
        return aiAnswerService.judgmentResume(resumeText);
    }

//    @ApiOperation("生成性格测试题")
//    @ResponseBody
//    @PostMapping("/generate/characteristicsTest")
//    public Result<CharacteristicsTestListVo> generateCharacteristicsTest(@RequestBody String resumeText) {
//        return aiAnswerService.generateCharacteristicsTest(resumeText);
//    }

    @ApiOperation("判断岗位是否存在")
    @ResponseBody
    @GetMapping("/judgment/position")
    public Result judgmentPosition(@RequestParam("position") String position){
        return aiAnswerService.judgmentPosition(position);
    }

    @ApiOperation("生成个人能力数据")
    @ResponseBody
    @PostMapping("/generate/personalAbility")
    public Result<PersonalAbilityImgVo> generatePersonalAbility(@RequestBody String resumeText){
        return aiAnswerService.generatePersonalAbility(resumeText);
    }

    @ApiOperation("生成测试题")
    @ResponseBody
    @GetMapping("/generate/testQuestion")
    public Result<TestQuestionListVo> generateTestQuestion(@RequestParam("jobId") String jobId,@RequestParam("position") String position){
        return aiAnswerService.generateTestQuestion(jobId,position);
    }

    @ApiOperation("分析测试结果")
    @ResponseBody
    @PostMapping("/testQuestion/result")
    public Result<PerformanceAnalysis> analysisResult(@RequestBody AnalysisResultDto dto){
        return aiAnswerService.analysisResult(dto);
    }

    @ApiOperation("推荐导师")
    @ResponseBody
    @PostMapping("/recommendation/mentor")
    public Result<EndResultVo> recommendationMentor(@RequestBody RecommendationMentorDto dto){
        return aiAnswerService.recommendationMentor(dto);
    }


    @ApiOperation("设置cookie")
    @ResponseBody
    @GetMapping("/setCookie")
    public void setCookie(HttpServletResponse response, String token, String url) {
        // 1. 配置跨域响应头（必须）
        response.setHeader("Access-Control-Allow-Origin", url);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // 2. 配置 Cookie（修复跨域写入问题）
        Cookie cookie = new Cookie("user_token", token);
        cookie.setHttpOnly(true); // 防止 XSS 攻击
        cookie.setMaxAge(1296000); // 15天有效期
        cookie.setPath("/"); // 全站生效
        // 本地开发用 Lax，生产环境用 None + Secure（必须 HTTPS）
//        cookie.setSameSite("Lax");
        // 生产环境开启，本地开发关闭（本地是 HTTP）
        // cookie.setSecure(true);

        // 3. 移除 sendRedirect，避免 302 重定向
        response.addCookie(cookie);
        // 直接返回成功状态，不再重定向
        response.setStatus(HttpServletResponse.SC_OK);
    }

//    @ResponseBody
//    @GetMapping("/ask/openai")
//    public Result askOpenAi(@RequestParam("question") String question){
//        String result = aiAnswerService.callWithMessage2(question);
//        return Result.success(result);
//    }
}
