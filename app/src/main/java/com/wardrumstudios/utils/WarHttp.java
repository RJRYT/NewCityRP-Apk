package com.wardrumstudios.utils;

public class WarHttp {
    protected WarHttp(WarBase warBase) {
        System.out.println("**** WarHttp::Init");
    }

    public void AddHttpGetLineFeeds(boolean z) {
        System.out.println("**** AddHttpGetLineFeeds");
    }

    public String HttpGet(String str) {
        System.out.println("**** HttpGet");
        return "";
    }

    public byte[] HttpGetData(String str) {
        System.out.println("**** HttpGetData");
        return null;
    }

    public String HttpPost(String str) {
        System.out.println("**** HttpPost");
        return "";
    }
}