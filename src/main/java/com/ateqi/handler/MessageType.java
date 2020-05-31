package com.ateqi.handler;

public interface MessageType {

    //成功
    public static String OK = "OK";

    //失败
    public static String NO = "NO";

    //心跳
    public static String HEAT_BEAT = "HEAT_BEAT";

    //初始化主题
    public static String INIT_TOPIC = "INIT_TOPIC";

    //订阅主题消息
    public static String SUB_TOPIC = "SUB_TOPIC";

    //取消订阅主题消息
    public static String CANCEL_SUB_TOPIC = "CANCEL_SUB_TOPIC";

    //发送消息
    public static String SEND_MSG = "SEND_MSG";
}
