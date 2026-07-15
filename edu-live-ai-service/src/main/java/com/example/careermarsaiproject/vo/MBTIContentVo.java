package com.example.careermarsaiproject.vo;

import com.example.careermarsaiproject.entity.ConstellationFoundationScore;
import com.example.careermarsaiproject.entity.MbtiQuestion;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "MBTITestQuestionListVo", description = "mbti测试题列表")
public class MBTIContentVo {
        private ConstellationFoundationScore constellationFoundationScore;
        private List<MbtiQuestion> mbtiQuestionList;
}
