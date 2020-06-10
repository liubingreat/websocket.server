package com.ateqi.dispatcher;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 订阅主题/取消订阅主题调度器
 */
public class AdminDispatcher {
    public static ConcurrentHashMap<String, CopyOnWriteArrayList<String>> topicMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();

    /**
     * 初始化主题集合
     * @param topic
     */
    public static int initTopic(String topic) {
        CopyOnWriteArrayList<String> list = topicMap.get(topic);
        if(list != null) {
            return 0;
        }else {
            topicMap.put(topic, new CopyOnWriteArrayList<String>());
            return 1;
        }
    }

    /**
     * 添加订阅主题的channel
     * @param topic
     * @param channelId
     * @return boolean
     */
    public static boolean subTopic(String topic, String channelId) {
        CopyOnWriteArrayList<String> list = topicMap.get(topic);
        if(null == list) {
            return false;
        }
        return list.add(channelId);
    }

    /**
     * 取消订阅主题的channelId
     * @param topic
     * @param channelId
     * @return boolean
     */
    public static void cancelSubTopic(String topic, String channelId) {
        if(null == topic) {
            Collection<CopyOnWriteArrayList<String>> values = topicMap.values();
            for(CopyOnWriteArrayList<String> value: values) {
                value.remove(channelId);
            }
        }else {
            CopyOnWriteArrayList<String> list = topicMap.get(topic);
            list.remove(channelId);
        }
    }

    /**
     * 获取订阅主题通道id集合
     * @param topic
     * @return
     */
    public static CopyOnWriteArrayList<String> getSubTopicChannelIds(String topic) {
        return topicMap.get(topic);
    }
}
