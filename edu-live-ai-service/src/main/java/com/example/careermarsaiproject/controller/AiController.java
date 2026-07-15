package com.example.careermarsaiproject.controller;

import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.dto.AnalysisResultDto;
import com.example.careermarsaiproject.dto.RecommendationMentorDto;
import com.example.careermarsaiproject.entity.MbtiResult;
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
    @Autowired
    private AiAnswerService aiAnswerService;

    @ApiOperation("解析简历文件")
    @ResponseBody
    @PostMapping("/parse/resume/file")
    public Result<ResumeVo> parseResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的文件!");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名称读取失败，请重新选择文件");
            }

            String contentType = file.getContentType();
            String fileName = originalFilename.toLowerCase();

            boolean isImage = contentType != null && contentType.startsWith("image/");
            boolean isPdf = fileName.endsWith(".pdf");
            boolean isDocx = fileName.endsWith(".docx") ||  fileName.endsWith(".doc");
            if (!isImage && !isPdf && !isDocx) {
                return Result.error("仅支持图片、PDF、doc、docx格式简历文件");
            }

            return aiAnswerService.parseFile(file);
        } catch (Exception e) {
            return Result.error("解析文件时发生错误: " + e.getMessage());
        }
    }

    @ApiOperation("查询mbti测试题和星座基础分")
    @ResponseBody
    @GetMapping("/search/mbti/content")
    public Result<MBTIContentVo> searchMBTIContent(String constellation) {
        return aiAnswerService.searchMBTIContent(constellation);
    }

    @ApiOperation("查询mbti测试结果")
    @ResponseBody
    @GetMapping("/search/mbti/result")
    public Result<MbtiResult> searchMBTIResult(String name) {
        return aiAnswerService.searchMBTIResult(name);
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
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @ApiOperation("保存学生咨询记录")
    @ResponseBody
    @GetMapping("/save/consultation/record")
    public Result<EndResultVo> saveConsultationRecord(@RequestParam("studentId") String studentId,@RequestParam("mentorId") String mentorId){
        return aiAnswerService.saveConsultationRecord(studentId,mentorId);
    }
}
