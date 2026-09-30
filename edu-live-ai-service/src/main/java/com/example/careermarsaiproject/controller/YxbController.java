package com.example.careermarsaiproject.controller;

import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.dto.*;
import com.example.careermarsaiproject.entity.*;
import com.example.careermarsaiproject.service.YxbService;
import com.example.careermarsaiproject.vo.YxbEndScoreVo;
import com.example.careermarsaiproject.vo.YxbJudgeScoreVo;
import com.example.careermarsaiproject.vo.YxbVoteRecordVo;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 毓秀杯评委 前端控制器
 * </p>
 *
 * @author 叶陵
 * @since 2026-09-10
 */
@Controller
@RequestMapping("/ai/yxb")
@Slf4j
public class YxbController {
    @Autowired
    private YxbService yxbService;

    @ResponseBody
    @GetMapping("/search/score")
    public Result<List<YxbScore>> searchScore(){
        return yxbService.searchScore();
    }

    @ResponseBody
    @PostMapping("/insert/score")
    public Result insertScore(@RequestBody List<YxbScoreDto> list){
        return yxbService.insertScore(list);
    }

    @ResponseBody
    @GetMapping("/reset/all")
    public Result resetAll(){
        return yxbService.resetAll();
    }

    @ResponseBody
    @GetMapping("/search/endScore")
    public Result<List<YxbEndScoreVo>> searchEndScore(){
        return yxbService.searchEndScore();
    }

    @ResponseBody
    @PostMapping("/current/endScore")
    public Result<List<YxbJudgeScoreVo>> searchCurrentScore(@RequestBody YxbJudges yxbJudge){
        return yxbService.searchCurrentScore(yxbJudge);
    }

    @ResponseBody
    @PostMapping("/update/status")
    public Result<Boolean> updateStatus(@RequestBody YxbJudgeScoreVo yxbJudgeScoreVo){
        return yxbService.updateStatus(yxbJudgeScoreVo);
    }

    @ResponseBody
    @PostMapping("/update/endScore")
    public Result<Boolean> updateEndScore(@RequestBody YxbEndScore yxbEndScore){
        return yxbService.updateEndScore(yxbEndScore);
    }

    @ResponseBody
    @PostMapping("/judges/login")
    public Result<YxbJudges> judgesLogin(@RequestBody YxbJudgesLoginDto dto){
        return yxbService.judgesLogin(dto);
    }

    @ResponseBody
    @GetMapping("/current/vote")
    public Result<List<YxbVote>> currentVote(){
        return yxbService.currentVote();
    }

    @ResponseBody
    @GetMapping("/current/vote/record")
    public Result<List<YxbVoteRecordVo>> currentVoteRecord(){
        return yxbService.currentVoteRecord();
    }

    @ResponseBody
    @PostMapping("/update/vote")
    public Result updateVote(@RequestBody YxbVoteReq yxbVoteReq){
        return yxbService.updateVote(yxbVoteReq);
    }

    @ResponseBody
    @PostMapping("/update/user")
    public Result updateUser(@RequestBody YxbUser yxbUser){
        return yxbService.updateUser(yxbUser);
    }


//    @ResponseBody
//    @ApiOperation("学生扫码登录-前端回调")
//    @GetMapping("/user/login")
//    public Result<YxbUser> userLogin(@RequestParam(value = "code") String code,
//                                     @RequestParam(value = "state") String state,
//                                     HttpServletResponse response) throws Exception {
//        try {
//            return yxbService.userLogin(code,state,response);
//        }catch (Exception e){
//            log.error("扫码登录回调异常",e);
//            return Result.error("登录内部异常："+e.getMessage());
//        }
//    }

    @ApiOperation("C端重置-短信-向新手机号发送重置手机号验证码")
    @ResponseBody
    @PostMapping("/send/code")
    public Result sendCode(@RequestBody SmsCodeDto dto) {
        return yxbService.sendCode(dto);
    }

    @ResponseBody
    @ApiOperation("学生登录")
    @PostMapping("/user/login")
    public Result<YxbUser> userLogin(@RequestBody UserLoginDto dto){
        return yxbService.userLogin(dto);
    }

    @ResponseBody
    @ApiOperation("发送弹幕")
    @PostMapping("/send/danmu")
    public Result sendDanmu(@RequestBody DanmuRecordDto dto){
        return yxbService.sendDanmu(dto);
    }

    @ResponseBody
    @ApiOperation("当前弹幕")
    @GetMapping("/current/danmu")
    public Result<List<YxbDanmuRecord>> currentDanmu(String lastId){
        return yxbService.currentDanmu(lastId);
    }

}
