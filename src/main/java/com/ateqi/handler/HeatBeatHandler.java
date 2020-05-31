package com.ateqi.handler;

import com.ateqi.dispatcher.AdminDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class HeatBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if(evt instanceof IdleStateEvent) {     //监听到超时没有读、写、读/写事件关闭处理器
            AdminDispatcher.cancelSubTopic(null, ctx.channel().id().asLongText());
            ctx.close();
        }
    }
}
