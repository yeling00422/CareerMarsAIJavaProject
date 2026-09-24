package com.example.careermarsaiproject.dto;

import com.example.careermarsaiproject.entity.YxbVote;
import com.example.careermarsaiproject.entity.YxbUser;
import lombok.Data;

@Data
public class YxbVoteReq {
    // 对应前端 item
    private YxbVote item;
    // 对应前端 userDate（用户信息）
    private YxbUser userDate;
}
