package com.example.careermarsaiproject.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "CharacteristicsTestListVo", description = "性格测试题列表")
public class CharacteristicsTestListVo {
        private List<CharacteristicsTestVo> questions;
}
