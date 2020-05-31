package com.ateqi.server;

import com.ateqi.handler.HeatBeatHandler;
import com.ateqi.handler.TextWebSocketFrameHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class WebSocketChannelInitializer extends ChannelInitializer {
    @Override
    protected void initChannel(Channel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("logging", new LoggingHandler("DEBUG"));
        socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
        socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        socketChannel.pipeline().addLast("websocket-handler", new WebSocketServerProtocolHandler("/websocket"));
        socketChannel.pipeline().addLast("", new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS));
        socketChannel.pipeline().addLast("handler", new TextWebSocketFrameHandler());
        socketChannel.pipeline().addLast("heatbeat", new HeatBeatHandler());
    }
}
