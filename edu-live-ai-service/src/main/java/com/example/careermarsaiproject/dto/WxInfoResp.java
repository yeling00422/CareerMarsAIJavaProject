package com.example.careermarsaiproject.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@Data
public class WxInfoResp {

    @ApiModelProperty(value = "会话标识", required = true)
    private String openid;

    @ApiModelProperty(value = "昵称", required = true)
    private String nickname;

    @ApiModelProperty(value = "性别", required = true)
    private Integer sex;

    @ApiModelProperty(value = "省份", required = true)
    private String province;

    @ApiModelProperty(value = "市", required = true)
    private String city;

    @ApiModelProperty(value = "国家", required = true)
    private String country;

    @ApiModelProperty(value = "头像地址", required = true)
    private String headimgurl;
//只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
    @ApiModelProperty(value = "unionid", required = true)
    private String unionid;
//    用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
    @ApiModelProperty(value = "用户特权信息", required = true)
    private List<String> privilege;
    @ApiModelProperty(value = "错误编码")
    private Integer errcode;
    @ApiModelProperty(value = "错误信息")
    private String errmsg;
    @ApiModelProperty(value = "rid")
    private String rid;
    @ApiModelProperty(value = "投票数量")
    private int voteCount;

//    openid	用户的唯一标识
//    nickname	用户昵称
//    sex	用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
//    province	用户个人资料填写的省份
//    city	普通用户个人资料填写的城市
//    country	国家，如中国为CN
//    headimgurl	用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
//    privilege	用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
//    unionid	只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。

}