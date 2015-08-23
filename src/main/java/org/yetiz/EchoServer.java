package org.yetiz;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory;

/**
 * Created by yeti on 2015/7/16.
 */
public class EchoServer {
	static {
		System.setProperty(YamlConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "log.yaml");
	}
	private COLAInitializer initializer = new COLAInitializer();
	private String ip = "0.0.0.0";
	private int port = 10315;
	private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
	private int backlog = 10240;

	public EchoServer() {
		init();
	}

	private void init() {
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			Channel channel = bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(initializer)
				.option(ChannelOption.SO_BACKLOG, backlog)
				.option(ChannelOption.SO_RCVBUF, 130172)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.bind(ip, port).sync().channel().closeFuture().sync().channel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class COLAInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline()
				.addLast(loggingHandler)
				.addLast(new EchoHandler())
			;
		}
	}
}
