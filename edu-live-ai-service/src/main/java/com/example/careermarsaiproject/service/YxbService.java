package com.example.careermarsaiproject.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.dto.WxInfoResp;
import com.example.careermarsaiproject.dto.YxbJudgesLoginDto;
import com.example.careermarsaiproject.dto.YxbScoreDto;
import com.example.careermarsaiproject.dto.YxbVoteReq;
import com.example.careermarsaiproject.entity.*;
import com.example.careermarsaiproject.utils.IdWorker;
import com.example.careermarsaiproject.utils.WxLoginConfig;
import com.example.careermarsaiproject.vo.YxbEndScoreVo;
import com.example.careermarsaiproject.vo.YxbJudgeScoreVo;
import com.example.careermarsaiproject.vo.YxbVoteRecordVo;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class YxbService {
    @Autowired
    private IYxbEndScoreService yxbEndScoreService;
    @Autowired
    private IYxbScoreService yxbScoreService;
    @Autowired
    private IYxbJudgesService yxbJudgesService;
    @Autowired
    private IYxbVoteService yxbVoteService;
    @Autowired
    private WxLoginConfig wxLoginConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IYxbUserService yxbUserService;
    @Autowired
    private IYxbVoteRecordService yxbVoteRecordService;


    public Result<List<YxbScore>> searchScore() {
        return Result.success(yxbScoreService.list());
    }

    public Result insertScore(List<YxbScoreDto> list) {
        List<YxbScore> yxbScoreList = yxbScoreService.list();
        if (yxbScoreList.size() != 0){
            yxbScoreService.removeByIds(yxbScoreList);
        }
        List<YxbScore> addList = new ArrayList<>();
        for (YxbScoreDto dto : list) {
            YxbScore yxbScore = new YxbScore();
            BeanUtils.copyProperties(dto,yxbScore);
            yxbScore.setId(IdWorker.getId().toString());
            yxbScore.setUpdateTime(LocalDateTime.now());
            if (dto.getScore1() == null) {
                yxbScore.setScore1(0.0);
            }
            if (dto.getScore2() == null) {
                yxbScore.setScore2(0.0);
            }
            if (dto.getScore3() == null) {
                yxbScore.setScore3(0.0);
            }
            if (dto.getScore4() == null) {
                yxbScore.setScore4(0.0);
            }
                addList.add(yxbScore);
            }
        boolean result = yxbScoreService.saveBatch(addList);
        if (result) {
            return Result.success();
        }else {
            return Result.error("更新失败！");
        }
    }

    public Result resetAll() {
        return Result.success(yxbScoreService.removeByIds(yxbScoreService.list()));
    }

    public Result<List<YxbEndScoreVo>> searchEndScore() {
        List<YxbEndScore> list = yxbEndScoreService.list();
        List<YxbEndScoreVo> voList = new ArrayList<>();
        for (YxbEndScore yxbEndScore : list) {
            YxbEndScoreVo vo = new YxbEndScoreVo();
            Integer expertScore1 = yxbEndScore.getExpertScore1();
            Integer expertScore2 = yxbEndScore.getExpertScore2();
            Integer expertScore3 = yxbEndScore.getExpertScore3();
            Integer expertScore4 = yxbEndScore.getExpertScore4();
            Integer expertScore5 = yxbEndScore.getExpertScore5();
            Integer volkswagenScore1 = yxbEndScore.getVolkswagenScore1();
            Integer volkswagenScore2 = yxbEndScore.getVolkswagenScore2();
            Integer volkswagenScore3 = yxbEndScore.getVolkswagenScore3();
            Integer volkswagenScore4 = yxbEndScore.getVolkswagenScore4();
            Integer volkswagenScore5 = yxbEndScore.getVolkswagenScore5();
            Integer volkswagenScore6 = yxbEndScore.getVolkswagenScore6();
            Integer volkswagenScore7 = yxbEndScore.getVolkswagenScore7();
            Integer volkswagenScore8 = yxbEndScore.getVolkswagenScore8();
            Integer volkswagenScore9 = yxbEndScore.getVolkswagenScore9();
            Integer volkswagenScore10 = yxbEndScore.getVolkswagenScore10();

            Double expertEndScore = null;
            Double volkswagenEndScore = null;
            Double endScore = null;

            //判断专家评委打分是否全部完成
            boolean expertAllNotNull = expertScore1 != null
                    && expertScore2 != null
                    && expertScore3 != null
                    && expertScore4 != null
                    && expertScore5 != null;
            //判断大众评委打分是否全部完成
            boolean volAllNotNull = volkswagenScore1 != null
                    && volkswagenScore2 != null
                    && volkswagenScore3 != null
                    && volkswagenScore4 != null
                    && volkswagenScore5 != null
                    && volkswagenScore6 != null
                    && volkswagenScore7 != null
                    && volkswagenScore8 != null
                    && volkswagenScore9 != null
                    && volkswagenScore10 != null;

            // 专家分计算
            if (expertAllNotNull) {
                int expertTotalScore = expertScore1 + expertScore2 + expertScore3 + expertScore4 + expertScore5;
                Double expertAvgScore = Double.valueOf(expertTotalScore) / 5;
                expertEndScore = Math.floor(expertAvgScore * 100) / 100.0;
            }
            vo.setExpertEndScore(expertEndScore);

            // 大众分计算
            if (volAllNotNull) {
                int volkswagenTotalScore = volkswagenScore1 + volkswagenScore2 + volkswagenScore3 + volkswagenScore4 + volkswagenScore5
                        + volkswagenScore6 + volkswagenScore7 + volkswagenScore8 + volkswagenScore9 + volkswagenScore10;
                Double volkswagenAvgScore = Double.valueOf(volkswagenTotalScore) / 10;
                volkswagenEndScore = Math.floor(volkswagenAvgScore * 100) / 100.0;
            }
            vo.setVolkswagenEndScore(volkswagenEndScore);

            // 两组都齐全才算最终分
            if (expertAllNotNull && volAllNotNull) {
                Double endAvgScore = (expertEndScore + volkswagenEndScore) / 2;
                endScore = Math.floor(endAvgScore * 100) / 100.0;
            }
            vo.setEndScore(endScore);
            vo.setName(yxbEndScore.getName());
            voList.add(vo);
        }
        return Result.success(voList);
    }

    public Result<List<YxbJudgeScoreVo>> searchCurrentScore(YxbJudges yxbJudge) {
        List<YxbEndScore> yxbEndScoreList = yxbEndScoreService.list();
        Map<String, String> judgeMap = yxbJudgesService.list().stream()
                .collect(Collectors.toMap(YxbJudges::getId, YxbJudges::getName));
        List<YxbJudgeScoreVo> voList = new ArrayList<>();
        for (YxbEndScore yxbEndScore : yxbEndScoreList) {
            YxbJudgeScoreVo vo = new YxbJudgeScoreVo();
            BeanUtils.copyProperties(yxbEndScore,vo);

            Integer expertScore1 = yxbEndScore.getExpertScore1();
            Integer expertScore2 = yxbEndScore.getExpertScore2();
            Integer expertScore3 = yxbEndScore.getExpertScore3();
            Integer expertScore4 = yxbEndScore.getExpertScore4();
            Integer expertScore5 = yxbEndScore.getExpertScore5();
            Integer volkswagenScore1 = yxbEndScore.getVolkswagenScore1();
            Integer volkswagenScore2 = yxbEndScore.getVolkswagenScore2();
            Integer volkswagenScore3 = yxbEndScore.getVolkswagenScore3();
            Integer volkswagenScore4 = yxbEndScore.getVolkswagenScore4();
            Integer volkswagenScore5 = yxbEndScore.getVolkswagenScore5();
            Integer volkswagenScore6 = yxbEndScore.getVolkswagenScore6();
            Integer volkswagenScore7 = yxbEndScore.getVolkswagenScore7();
            Integer volkswagenScore8 = yxbEndScore.getVolkswagenScore8();
            Integer volkswagenScore9 = yxbEndScore.getVolkswagenScore9();
            Integer volkswagenScore10 = yxbEndScore.getVolkswagenScore10();
            //判断专家评委打分是否全部完成
            boolean expertAllNotNull = expertScore1 != null
                    && expertScore2 != null
                    && expertScore3 != null
                    && expertScore4 != null
                    && expertScore5 != null;
            //判断大众评委打分是否全部完成
            boolean volAllNotNull = volkswagenScore1 != null
                    && volkswagenScore2 != null
                    && volkswagenScore3 != null
                    && volkswagenScore4 != null
                    && volkswagenScore5 != null
                    && volkswagenScore6 != null
                    && volkswagenScore7 != null
                    && volkswagenScore8 != null
                    && volkswagenScore9 != null
                    && volkswagenScore10 != null;

            Double expertEndScore = null;
            Double volkswagenEndScore = null;
            Double endScore = null;

            // 两组都齐全才算最终分
            if (expertAllNotNull && volAllNotNull) {
                int expertTotalScore = expertScore1 + expertScore2 + expertScore3 + expertScore4 + expertScore5;
                Double expertAvgScore = Double.valueOf(expertTotalScore) / 5;
                expertEndScore = Math.floor(expertAvgScore * 100) / 100.0;

                int volkswagenTotalScore = volkswagenScore1 + volkswagenScore2 + volkswagenScore3 + volkswagenScore4 + volkswagenScore5
                        + volkswagenScore6 + volkswagenScore7 + volkswagenScore8 + volkswagenScore9 + volkswagenScore10;
                Double volkswagenAvgScore = Double.valueOf(volkswagenTotalScore) / 10;
                volkswagenEndScore = Math.floor(volkswagenAvgScore * 100) / 100.0;

                Double endAvgScore = (expertEndScore + volkswagenEndScore) / 2;
                endScore = Math.floor(endAvgScore * 100) / 100.0;
                vo.setEndScore(endScore);
            }
            vo.setExpertName1(judgeMap.get(yxbEndScore.getExpertId1()));
            vo.setExpertName2(judgeMap.get(yxbEndScore.getExpertId2()));
            vo.setExpertName3(judgeMap.get(yxbEndScore.getExpertId3()));
            vo.setExpertName4(judgeMap.get(yxbEndScore.getExpertId4()));
            vo.setExpertName5(judgeMap.get(yxbEndScore.getExpertId5()));
            vo.setVolkswagenName1(judgeMap.get(yxbEndScore.getVolkswagenId1()));
            vo.setVolkswagenName2(judgeMap.get(yxbEndScore.getVolkswagenId2()));
            vo.setVolkswagenName3(judgeMap.get(yxbEndScore.getVolkswagenId3()));
            vo.setVolkswagenName4(judgeMap.get(yxbEndScore.getVolkswagenId4()));
            vo.setVolkswagenName5(judgeMap.get(yxbEndScore.getVolkswagenId5()));
            vo.setVolkswagenName6(judgeMap.get(yxbEndScore.getVolkswagenId6()));
            vo.setVolkswagenName7(judgeMap.get(yxbEndScore.getVolkswagenId7()));
            vo.setVolkswagenName8(judgeMap.get(yxbEndScore.getVolkswagenId8()));
            vo.setVolkswagenName9(judgeMap.get(yxbEndScore.getVolkswagenId9()));
            vo.setVolkswagenName10(judgeMap.get(yxbEndScore.getVolkswagenId10()));
            voList.add(vo);
        }
        return Result.success(voList);
    }

    public Result<Boolean> updateStatus(YxbJudgeScoreVo yxbJudgeScoreVo) {
        YxbEndScore yxbEndScore = yxbEndScoreService.getById(yxbJudgeScoreVo.getId());
        if (yxbJudgeScoreVo.getStatus() == 1){
            yxbEndScore.setStatus(0);
        }else {
            yxbEndScore.setStatus(1);
        }
        return Result.success(yxbEndScoreService.updateById(yxbEndScore));
    }

    public Result<Boolean> updateEndScore(YxbEndScore yxbEndScore) {
        if (yxbEndScore.getStatus() == 0){
            return Result.error("不能修改禁用状态的选手得分！请先联系管理员修改状态！");
        }
        return Result.success(yxbEndScoreService.updateById(yxbEndScore));
    }

    public Result<YxbJudges> judgesLogin(YxbJudgesLoginDto dto) {
        if (StringUtils.isEmpty(dto.getAccount()) || StringUtils.isEmpty(dto.getPassword())){
            return Result.error("账号或者密码不能为空");
        }
        LambdaQueryWrapper<YxbJudges> queryWrapper = new LambdaQueryWrapper<YxbJudges>()
                .eq(YxbJudges::getAccountNumber,dto.getAccount())
                .eq(YxbJudges::getPassword,dto.getPassword());;
        YxbJudges yxbJudges = yxbJudgesService.getOne(queryWrapper);
        if (yxbJudges == null){
            return Result.error("账号或密码错误！请重新输入");
        }
        return Result.success(yxbJudges);
    }

    public Result<List<YxbVote>> currentVote() {
        return Result.success(yxbVoteService.list());
    }

    @Transactional
    public Result updateVote(YxbVoteReq yxbVoteReq) {
        YxbVote yxbVote = yxbVoteReq.getItem();
        YxbUser userDate = yxbVoteReq.getUserDate();
        String userId = userDate.getId();

        boolean updateSuccess = yxbUserService.lambdaUpdate()
                .eq(YxbUser::getId, userId)
                .gt(YxbUser::getVoteCount, 0)
                .setSql("vote_count = vote_count - 1")
                .set(YxbUser::getUpdateTime, LocalDateTime.now())
                .update();

        if(!updateSuccess){
            return Result.error("您的票数已用完！");
        }

        yxbVoteService.lambdaUpdate()
                .eq(YxbVote::getId, yxbVote.getId())
                .setSql("vote_number = vote_number + 1")
                .update();

        YxbVoteRecord yxbVoteRecord = new YxbVoteRecord();
        yxbVoteRecord.setVoteId(yxbVote.getId());
        yxbVoteRecord.setUserId(userId);
        yxbVoteRecord.setCreateTime(LocalDateTime.now());
        yxbVoteRecord.setUpdateTime(LocalDateTime.now());
        yxbVoteRecordService.save(yxbVoteRecord);

        YxbUser yxbUser = yxbUserService.getById(userId);
        return Result.success(yxbUser);
    }


    public Result<YxbUser> userLogin(String code, String state, HttpServletResponse response) {
        String url = wxLoginConfig.tokenUrl.replace("APPID", wxLoginConfig.appId).replace("SECRET", wxLoginConfig.appSecret).replace("CODE", code);
        String result = restTemplate.getForEntity(url, String.class).getBody();
        JSONObject loginResult = JSONUtil.parseObj(result);

        if(loginResult.containsKey("errcode")){
            log.error("微信code换取openid失败，errcode:{},errmsg:{}", loginResult.getStr("errcode"), loginResult.getStr("errmsg"));
            return Result.error("微信授权凭证失效，请重新扫码");
        }

        WxToken wxToken = new WxToken();
        wxToken.setAccessToken(loginResult.getStr("access_token"));
        wxToken.setOpenid(loginResult.getStr("openid"));
        wxToken.setExpiresIn(loginResult.getStr("expires_in"));
        wxToken.setUnionid(loginResult.getStr("unionid"));
        wxToken.setScope(loginResult.getStr("scope"));
        wxToken.setRefreshToken(loginResult.getStr("refresh_token"));

        String wxUrl = wxLoginConfig.userInfoUrl.replace("ACCESS_TOKEN", wxToken.getAccessToken()).replace("OPENID", wxToken.getOpenid());
        String userInfoJsonStr = restTemplate.getForEntity(wxUrl, String.class).getBody();
        userInfoJsonStr = new String(userInfoJsonStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("转码后微信原始返回: {}", userInfoJsonStr);
        JSONObject jsonObj = JSONUtil.parseObj(userInfoJsonStr);
        WxInfoResp wxInfoResp = JSONUtil.toBean(jsonObj, WxInfoResp.class);
        
//        WxInfoResp wxInfoResp = new WxInfoResp();
//        wxInfoResp.setOpenid("ogAEl6ebh5ExGB3kX-82PtdJvAIg");
//        wxInfoResp.setUnionid("oKz8N603alPlxYoAws-PhqohD5k0");
//        wxInfoResp.setNickname("叶陵");
//        wxInfoResp.setHeadimgurl("https://thirdwx.qlogo.cn/mmopen/vi_32/NfSUuFVlu2dSwO1let2MTGTtjU46AoUHrQuLy9icQeg8s0ExJTaYgGH0bwxIOt5EibgGCpeOhU04a7B4Yuw99VrKjuKh3vJlmJTRnkqU05l3I/132");
//        wxInfoResp.setSex(0);
//        wxInfoResp.setProvince("");
//        wxInfoResp.setCity("");
//        wxInfoResp.setCountry("");

        YxbUser yxbUser = yxbUserService.getOne(new LambdaQueryWrapper<YxbUser>()
                .eq(YxbUser::getOpenId, wxInfoResp.getOpenid()));
        if (yxbUser == null){
            yxbUser = new YxbUser();
            BeanUtils.copyProperties(wxInfoResp, yxbUser);
            yxbUser.setId(IdWorker.getId().toString());
            yxbUser.setOpenId(wxInfoResp.getOpenid());
            yxbUser.setUnionId(wxInfoResp.getUnionid());
            yxbUser.setNickName(wxInfoResp.getNickname());
            yxbUser.setHeadImg(wxInfoResp.getHeadimgurl());
            yxbUser.setProvice(wxInfoResp.getProvince());
            yxbUser.setVoteCount(3);
            yxbUser.setCreateTime(LocalDateTime.now());
            yxbUser.setUpdateTime(LocalDateTime.now());
            boolean saveResult = yxbUserService.save(yxbUser);
            if (saveResult){
                return Result.success(yxbUser);
            }else {
                return Result.error("用户创建失败!");
            }
        }else {
            return Result.success(yxbUser);
        }
    }

    public Result<List<YxbVoteRecordVo>> currentVoteRecord() {
        Map<String, String> voteMap = yxbVoteService.list().stream()
                .collect(Collectors.toMap(YxbVote::getId,YxbVote::getName));

        Map<String, String> userMap = yxbUserService.list().stream()
                .collect(Collectors.toMap(YxbUser::getId,YxbUser::getNickName));

        List<YxbVoteRecord> recordList = yxbVoteRecordService.list();
        List<YxbVoteRecordVo> voList = new ArrayList<>();
        for (YxbVoteRecord yxbVoteRecord : recordList) {
            YxbVoteRecordVo vo = new YxbVoteRecordVo();
            BeanUtils.copyProperties(yxbVoteRecord,vo);
            vo.setVoteName(voteMap.get(yxbVoteRecord.getVoteId()));
            vo.setUserName(userMap.get(yxbVoteRecord.getUserId()));
            voList.add(vo);
        }
        return Result.success(voList);
    }
}
