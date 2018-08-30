package com.otpsync.listener;

public class LinkedDevice {
    private String token;
    private String name;

    public LinkedDevice(String qrcode) {
        String[] split = qrcode.split("\t");
        this.token = split[1];
        this.name = split[0];
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
