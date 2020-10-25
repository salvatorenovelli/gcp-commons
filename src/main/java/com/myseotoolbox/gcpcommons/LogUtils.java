package com.myseotoolbox.gcpcommons;

public class LogUtils {
    public static final int MAX_LOG_MESSAGE_LEN = 200;


    public static String shortToString(String val) {
        if (val == null || val.length() < MAX_LOG_MESSAGE_LEN) return val;
        return val.substring(0, MAX_LOG_MESSAGE_LEN);
    }
}
