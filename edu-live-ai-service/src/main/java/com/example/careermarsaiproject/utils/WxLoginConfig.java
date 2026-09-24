package com.example.careermarsaiproject.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WxLoginConfig {
    @Value("${wx.ggpt.appId}")
    public  String appId;
    @Value("${wx.ggpt.appSecret}")
    public  String appSecret;
    @Value("${wx.ggpt.tokenUrl}")
    public String tokenUrl;

    @Value("${wx.ggpt.userInfoUrl}")
    public String userInfoUrl;
}
