package com.ateqi.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 通道管理器
 */
public class ChannelSupervise {
    private   static ChannelGroup GlobalGroup= new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private  static ConcurrentMap<String, ChannelId> ChannelMap = new ConcurrentHashMap();

    /**
     * 添加通道
     * @param channel
     */
    public  static void addChannel(Channel channel){
        GlobalGroup.add(channel);
        ChannelMap.put(channel.id().asLongText(),channel.id());
    }

    /**
     * 移除通道
     * @param channel
     */
    public static void removeChannel(Channel channel){
        GlobalGroup.remove(channel);
        ChannelMap.remove(channel.id().asLongText());
    }

    /**
     * 查找通道
     * @param id
     * @return
     */
    public static  Channel findChannel(String id){
        return GlobalGroup.find(ChannelMap.get(id));
    }

    /**
     * 全局广播
     * @param tws
     */
    public static void send2All(TextWebSocketFrame tws){
        GlobalGroup.writeAndFlush(tws);
    }

    /**
     * 发送Text到指定通道
     * @param tws
     * @param targetChannelId
     */
    public static void sendToChannel(TextWebSocketFrame tws, final String targetChannelId) {
        Channel channel = findChannel(targetChannelId);
        channel.writeAndFlush(tws);
    }
}
