package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "YxbEndScoreVo", description = "毓秀杯最终得分")
public class YxbJudgeScoreVo {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 选手名称
     */
    private String name;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 作品
     */
    private String work;

    /**
     * 状态 0-禁用 1-启用
     */
    private int status;

    // ========== 专家评委1~5 ==========
    /**
     * 专家评委1
     */
    private String expertId1;
    private Integer expertScore1;
    private String expertName1;

    /**
     * 专家评委2
     */
    private String expertId2;
    private Integer expertScore2;
    private String expertName2;

    /**
     * 专家评委3
     */
    private String expertId3;
    private Integer expertScore3;
    private String expertName3;

    /**
     * 专家评委4
     */
    private String expertId4;
    private Integer expertScore4;
    private String expertName4;

    /**
     * 专家评委5
     */
    private String expertId5;
    private Integer expertScore5;
    private String expertName5;


    // ========== 大众评委1~10 ==========
    /**
     * 大众评委1
     */
    private String volkswagenId1;
    private Integer volkswagenScore1;
    private String volkswagenName1;

    /**
     * 大众评委2
     */
    private String volkswagenId2;
    private Integer volkswagenScore2;
    private String volkswagenName2;

    /**
     * 大众评委3
     */
    private String volkswagenId3;
    private Integer volkswagenScore3;
    private String volkswagenName3;

    /**
     * 大众评委4
     */
    private String volkswagenId4;
    private Integer volkswagenScore4;
    private String volkswagenName4;

    /**
     * 大众评委5
     */
    private String volkswagenId5;
    private Integer volkswagenScore5;
    private String volkswagenName5;

    /**
     * 大众评委6
     */
    private String volkswagenId6;
    private Integer volkswagenScore6;
    private String volkswagenName6;

    /**
     * 大众评委7
     */
    private String volkswagenId7;
    private Integer volkswagenScore7;
    private String volkswagenName7;

    /**
     * 大众评委8
     */
    private String volkswagenId8;
    private Integer volkswagenScore8;
    private String volkswagenName8;

    /**
     * 大众评委9
     */
    private String volkswagenId9;
    private Integer volkswagenScore9;
    private String volkswagenName9;

    /**
     * 大众评委10
     */
    private String volkswagenId10;
    private Integer volkswagenScore10;
    private String volkswagenName10;

    private Double endScore;
}