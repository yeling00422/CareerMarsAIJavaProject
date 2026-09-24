package com.example.careermarsaiproject.entity;

import lombok.Data;

@Data
public class WxToken {

    //    接口调用凭证
    private String accessToken;

    //    access_token接口调用凭证超时时间，单位（秒）
    private String expiresIn;

    //    用户刷新access_token
    private String refreshToken;

    //    授权用户唯一标识
    private String openid;

    //    用户授权的作用域，使用逗号（,）分隔
    private String scope;

    //    用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的
    private String unionid;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }
}
