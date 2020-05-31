package com.ateqi.handler;

import com.alibaba.fastjson.JSONObject;
import com.ateqi.dispatcher.AdminDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.concurrent.CopyOnWriteArrayList;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            handlWebSocketFrame(ctx, msg);
        }catch (Exception e) {
            JSONObject res = new JSONObject();
            res.put("type", MessageType.NO);
            ctx.channel().writeAndFlush(res.toJSONString());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelSupervise.addChannel(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ChannelSupervise.removeChannel(ctx.channel());
        AdminDispatcher.cancelSubTopic(null, ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AdminDispatcher.cancelSubTopic(null, ctx.channel().id().asLongText());
        ctx.close();
        throw new Exception(cause);
    }


    public void handlWebSocketFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        // 返回应答消息
        String request =  frame.text();
        JSONObject msgObj = (JSONObject)JSONObject.parse(request);
        String type = (String)msgObj.get("type");
        String topic = (String)msgObj.get("topic");
        String channnelId = ctx.channel().id().asLongText();
        TextWebSocketFrame tws = null;
        JSONObject res = new JSONObject();
        switch (type) {
            case MessageType.SEND_MSG :  //消息传输(topicClient)
                CopyOnWriteArrayList<String> ids = AdminDispatcher.getSubTopicChannelIds(topic);
                if(null != ids) {
                    for(int i = 0, len = ids.size(); i < len; i++) {
                        String id = ids.get(i);
                        TextWebSocketFrame msg = new TextWebSocketFrame(frame.text());
                        ChannelSupervise.sendToChannel(msg, id);
                    }
                }
                break;
            case MessageType.SUB_TOPIC :  //订阅消息(subClient)
                AdminDispatcher.subTopic(topic, channnelId);
                res.put("type", MessageType.OK);
                res.put("topic", topic);
                tws = new TextWebSocketFrame(res.toJSONString());
                ctx.channel().writeAndFlush(tws);
                break;
            case MessageType.CANCEL_SUB_TOPIC :  //取消订阅消息(subClient)
                AdminDispatcher.cancelSubTopic(topic, ctx.channel().id().asLongText());
                res.put("type", MessageType.OK);
                res.put("topic", topic);
                tws = new TextWebSocketFrame(res.toJSONString());
                ctx.channel().writeAndFlush(tws);
                break;
            case MessageType.INIT_TOPIC :  //发布消息(topicClient)
                AdminDispatcher.initTopic(topic);
                res.put("type", MessageType.OK);
                res.put("topic", topic);
                tws = new TextWebSocketFrame(res.toJSONString());
                ctx.channel().writeAndFlush(tws);
                break;
            case MessageType.HEAT_BEAT:     //心跳检测
                res.put("type", MessageType.HEAT_BEAT);
                tws = new TextWebSocketFrame(res.toJSONString());
                ctx.channel().writeAndFlush(tws);
                break;
        }
    }
}
