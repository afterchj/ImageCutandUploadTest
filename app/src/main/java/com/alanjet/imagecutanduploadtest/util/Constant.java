package com.alanjet.imagecutanduploadtest.util;

/**
 * Created by hongjian.chen on 2018/6/13.
 */

public enum Constant {
    HEAD("http://www.uichange.com/ums3-share/user/14715689.jpg"),
    WEB_SSM("https://github.com/afterchj/web-ssm"),
    DEFAULT("http://www.uichange.com/ums3-share/"),
    UMS3_CLIENT2("http://www.uichange.com/ums3-client2/heads/");

    private String baseUrl;

    Constant(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

}
