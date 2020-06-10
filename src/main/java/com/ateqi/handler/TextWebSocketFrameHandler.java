package com.ateqi.handler;

import com.alibaba.fastjson.JSONObject;
import com.ateqi.common.CommonLogFactory;
import com.ateqi.dispatcher.AdminDispatcher;
import com.ateqi.entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.concurrent.CopyOnWriteArrayList;

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<Object> {
    private Logger plog = CommonLogFactory.getProcessDataLog();
    private Logger elog = CommonLogFactory.getExceptionLog();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            if(msg instanceof TextWebSocketFrame) {
                handlWebSocketFrame(ctx, (TextWebSocketFrame)msg);
            }else if(msg instanceof FullHttpRequest) {
                handlHttpRequest(ctx, (FullHttpRequest)msg);
            }
        }catch (Exception e) {
            JSONObject res = new JSONObject();
            res.put("type", MessageType.NO);
            ctx.channel().writeAndFlush(res.toJSONString());
            elog.error("解析消息出错:" + res.toJSONString());
            elog.error("错误消息详情:" , e);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelSupervise.addChannel(ctx.channel());
        plog.info("客户端上线:" + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ChannelSupervise.removeChannel(ctx.channel());
        AdminDispatcher.cancelSubTopic(null, ctx.channel().id().asLongText());
        plog.info("客户端掉线:" + ctx.channel().remoteAddress().toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AdminDispatcher.cancelSubTopic(null, ctx.channel().id().asLongText());
        ctx.close();
        elog.error("出现异常:" + ctx.channel().remoteAddress().toString());
        elog.error("异常详情:", cause);
        throw new Exception(cause);
    }

    /**
     * 处理TextWebsocketFrame
     * @param ctx
     * @param frame
     */
    public void handlWebSocketFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try{
            Message reciveMessage = JSONObject.parseObject(frame.text(), Message.class);
            Message resMessage = handlMessage(ctx, reciveMessage);
            ctx.writeAndFlush(new TextWebSocketFrame(resMessage.toString()));
        } catch (Exception e){
            elog.error("消息解析错误", e);
            return ;
        }

    }

    /**
     * 处理http请求
     * @param ctx
     * @param request
     */
    public void handlHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        ByteBuf content = request.content();
        Charset charset = Charset.forName("UTF-8");
        HttpVersion version = HttpVersion.HTTP_1_1;
        HttpMethod method = request.method();
        FullHttpResponse res = null;
        ByteBuf data = null;
        HttpResponseStatus status = HttpResponseStatus.OK;
        try{
            if(uri.contains("/message")) {
                try {
                    if(HttpMethod.POST == method) {
                        Message message = JSONObject.parseObject(content.toString(charset), Message.class);
                        Message resMessage = handlMessage(ctx, message);
                        data = Unpooled.copiedBuffer(resMessage.toString(), charset);
                    }else {
                        status = HttpResponseStatus.FORBIDDEN;
                        data = Unpooled.copiedBuffer("只允许发送POST请求", charset);
                    }
                } catch(Exception e) {
                    elog.error("消息解析错误", e);
                    data = Unpooled.copiedBuffer(e.getMessage(), charset);
                }

            }else if(uri.contains("/test")) {
                data = Unpooled.copiedBuffer("OK", charset);
            }else {
                status = HttpResponseStatus.NOT_FOUND;
                data = Unpooled.copiedBuffer("Not Found", charset);
            }
            res = new DefaultFullHttpResponse(version, status, data);
        } catch(Exception e) {
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            data = Unpooled.copiedBuffer("Internal Server Error" + e.getLocalizedMessage(), charset);
            res = new DefaultFullHttpResponse(version, status, data);
        }
        HttpUtil.setKeepAlive(res, HttpUtil.isKeepAlive(request));
        HttpUtil.setContentLength(res, data.readableBytes());
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        ctx.writeAndFlush(res);
    }

    /**
     * 消息处理
     * @param ctx
     * @param reciveMessage
     * @return
     */
    public Message handlMessage(ChannelHandlerContext ctx, Message reciveMessage) {
        String type = reciveMessage.getType();
        String topic = reciveMessage.getTopic();
        String channelId = ctx.channel().id().asLongText();
        Message responseMessage = new Message();
        switch (type) {
            case MessageType.SEND_MSG :  //消息传输(topicClient)
                CopyOnWriteArrayList<String> ids = AdminDispatcher.getSubTopicChannelIds(topic);
                if(null != ids) {
                    for(int i = 0, len = ids.size(); i < len; i++) {
                        String id = ids.get(i);
                        TextWebSocketFrame msg = new TextWebSocketFrame(reciveMessage.toString());
                        ChannelSupervise.sendToChannel(msg, id);
                    }
                }
                responseMessage.setType(MessageType.OK);
                responseMessage.setTopic(topic);
                plog.info("发送消息:" + reciveMessage.toString());
                break;
            case MessageType.SUB_TOPIC :  //订阅消息(subClient)
                boolean subSuccess = AdminDispatcher.subTopic(topic, channelId);
                responseMessage.setTopic(topic);
                if(subSuccess) {
                    responseMessage.setType(MessageType.OK);
                    plog.info("订阅主题成功:" + reciveMessage.toString() + ", 客户端：" + ctx.channel().remoteAddress());
                }else {
                    responseMessage.setType(MessageType.NO);
                    responseMessage.setTopic(topic);
                    responseMessage.setBody("暂时没有该主题");
                    plog.info("订阅主题失败:" + reciveMessage.toString() + ", 客户端：" + ctx.channel().remoteAddress());
                }
                break;
            case MessageType.CANCEL_SUB_TOPIC :  //取消订阅消息(subClient)
                AdminDispatcher.cancelSubTopic(topic, ctx.channel().id().asLongText());
                responseMessage.setType(MessageType.OK);
                responseMessage.setTopic(topic);
                plog.info("取消订阅主题:" + reciveMessage.toString());
                break;
            case MessageType.INIT_TOPIC :  //发布消息(topicClient)
                AdminDispatcher.initTopic(topic);
                responseMessage.setType(MessageType.OK);
                responseMessage.setTopic(topic);
                plog.info("初始化主题:" + reciveMessage.toString());
                break;
            case MessageType.HEAT_BEAT:     //心跳检测
                responseMessage.setType(MessageType.HEAT_BEAT);
                break;
        }
        return responseMessage;
    }

}
