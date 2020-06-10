package com.ateqi.server;

import com.ateqi.common.CommonLogFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;

public class Server {
    private Logger processDataLog = CommonLogFactory.getProcessDataLog();
    private Logger errorLog = CommonLogFactory.getExceptionLog();
    private int port;
    public Server(int port) {
        this.port = port;
    }

    /**
     * 启动服务
     */
    public void start() {
        processDataLog.info("服务正在启动...");
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(8);
        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new HttpServerCodec())
                    .handler(new ChunkedWriteHandler())
                    .handler(new HttpObjectAggregator(1024*1024*64))
                    .childHandler(new WebSocketChannelInitializer());
            Channel channel = b.bind(this.port).sync().channel();
            processDataLog.info("服务启动成功...");
            channel.closeFuture().sync();
        }catch (InterruptedException e) {
            errorLog.error("服务器启动失败:", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            errorLog.error("服务器停止运行");
        }
    }

}
