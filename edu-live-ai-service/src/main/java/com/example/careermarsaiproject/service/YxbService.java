package com.example.careermarsaiproject.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.careermarsaiproject.base.Result;
import com.example.careermarsaiproject.dto.YxbJudgesLoginDto;
import com.example.careermarsaiproject.dto.YxbScoreDto;
import com.example.careermarsaiproject.entity.YxbEndScore;
import com.example.careermarsaiproject.entity.YxbJudges;
import com.example.careermarsaiproject.entity.YxbScore;
import com.example.careermarsaiproject.utils.IdWorker;
import com.example.careermarsaiproject.vo.YxbEndScoreVo;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YxbService {
    @Autowired
    private IYxbEndScoreService yxbEndScoreService;
    @Autowired
    private IYxbScoreService yxbScoreService;
    @Autowired
    private IYxbJudgesService yxbJudgesService;
    //    public Result<List<YxbEndScore>> listRank() {
//        LambdaQueryWrapper<YxbEndScore> wrapper = new LambdaQueryWrapper<>();
//        wrapper.orderByDesc(YxbEndScore::getExpert, YxbEndScore::getVolkswagen);
//
//        List<YxbEndScore> list = yxbEndScoreService.list(wrapper);
//        return Result.success(list);
//    }
//
//    public void saveCurrentScore(YxbScoreDTO dto) {
//        // 逻辑：始终只保留1条记录，每次提交直接清空旧数据，新增一条
//        LambdaQueryWrapper<YxbScore> queryWrapper = new LambdaQueryWrapper<YxbScore>()
//                .eq(YxbScore::getName, dto.getName())
//                .eq(YxbScore::getWork, dto.getWork());;
//
//        yxbScoreService.remove(queryWrapper);
//        YxbScore yxbScore = new YxbScore();
//        yxbScore.setName(dto.getName());
//        yxbScore.setWork(dto.getWork());
//        yxbScore.setScore1(dto.getScore1());
//        yxbScore.setScore2(dto.getScore2());
//        yxbScore.setScore3(dto.getScore3());
//        yxbScore.setScore4(dto.getScore4());
//        yxbScore.setUpdateTime(LocalDateTime.now());
//        yxbScoreService.save(yxbScore);
//    }
//
//    // 获取大屏当前展示分数
//    public YxbScore getCurrentScore() {
//        LambdaQueryWrapper<YxbScore> wrapper = new LambdaQueryWrapper<>();
//        wrapper.orderByDesc(YxbScore::getId) // 按主键倒序，拿最新一条
//                .last("limit 1");
//        return yxbScoreService.getOne(wrapper);
//    }

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
            if (yxbEndScore.getExpert() != null && yxbEndScore.getExpert() != -1 &&
                    yxbEndScore.getVolkswagen() != null && yxbEndScore.getVolkswagen() != -1) {
                int expert = (int) (yxbEndScore.getExpert() / 5);
                int volkswagen = (int) (yxbEndScore.getVolkswagen() / 10);
                int score = (int) ((expert + volkswagen) / 2);
                vo.setExpertScore(yxbEndScore.getExpert());
                vo.setVolkswagenScore(yxbEndScore.getVolkswagen());
                vo.setEndScore(score);
            }else {
                vo.setEndScore(-1);
                if (yxbEndScore.getExpert() != null){
                    vo.setExpertScore(yxbEndScore.getExpert());
                }else {
                    vo.setExpertScore(-1);
                }
                if (yxbEndScore.getVolkswagen() != null){
                    vo.setVolkswagenScore(yxbEndScore.getVolkswagen());
                }else {
                    vo.setVolkswagenScore(-1);
                }
            }
            vo.setName(yxbEndScore.getName());
            voList.add(vo);
        }
        return Result.success(voList);
    }

    public Result<List<YxbEndScore>> searchCurrentScore() {
        return Result.success(yxbEndScoreService.list());
    }

    public Result<Boolean> updateEndScore(YxbEndScore yxbEndScore) {
        return Result.success(yxbEndScoreService.updateById(yxbEndScore));
    }

    public Result judgesLogin(YxbJudgesLoginDto dto) {
        if (StringUtils.isEmpty(dto.getAccountNumber()) || StringUtils.isEmpty(dto.getPassword())){
            return Result.error("账号或者密码不能为空");
        }
        LambdaQueryWrapper<YxbJudges> queryWrapper = new LambdaQueryWrapper<YxbJudges>()
                .eq(YxbJudges::getAccountNumber,dto.getAccountNumber())
                .eq(YxbJudges::getPassword,dto.getPassword());;
        YxbJudges yxbJudges = yxbJudgesService.getOne(queryWrapper);
        if (yxbJudges == null){
            return Result.error("账号或密码错误！请重新输入");
        }
        return Result.success(yxbJudges);
    }
}
