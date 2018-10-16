package carmelo.examples.client;
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import carmelo.common.Configuration;
import carmelo.common.SpringContextHolder;
import carmelo.examples.client.business.UserIdentify;
import carmelo.examples.handler.TcpClientDecoder;
import carmelo.examples.handler.TcpClientEncoder;
import carmelo.examples.handler.TcpClientHandler;
import carmelo.servlet.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * an tcp client example
 * 
 * @author needmorecode
 * Tcp客户端主程序入口类
 *
 */
public class TcpClientMain extends Thread{

	private  static String host;
	private static int port;
	private static int firstMessageSize;
	private static ChannelHandlerContext ctx;
	private static TcpClientMain client;
	private Channel channel;
	//latch用于主线程同步等待建立连接
	private static CountDownLatch latch = new CountDownLatch(1);

	public static TcpClientMain getInstance() {
		return client;
	}
	
	private TcpClientMain(String host, int port, int firstMessageSize) {
		TcpClientMain.host = host;
		TcpClientMain.port = port;
		TcpClientMain.firstMessageSize = firstMessageSize;
	}

	public Channel connect(String host, int port) {
		doConnect(host, port);
		return this.channel;
	}

	public void doConnect(String host, int port) {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.option(ChannelOption.TCP_NODELAY, true);
//			b.option(ChannelOption.TCP_NODELAY, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new TcpClientDecoder()); //自定义解码器
						ch.pipeline().addLast(new TcpClientEncoder());//自定义编码器
						ch.pipeline().addLast(new TcpClientHandler(firstMessageSize, TcpClientMain.this));//业务处理handler
					}
			});

			ChannelFuture f = b.connect(host, port);
			f.addListener(new ConnectionListener());
			channel = f.channel();
		} catch(Exception e) {
			e.printStackTrace();
		} 
		//因为线程没有像平时那样在连接建立后阻塞等待连接关闭，所以下面的finally语句不能有，否则连接会关闭
		//这样做的目的是为了实现掉线重连功能，在clientHandler.inActive()里调用
//		finally {
//			workerGroup.shutdownGracefully();
//		}
	}

	//放在单独的线程里跑
	public void run() {
		doConnect( host, port);
	}



	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		final String host = "127.0.0.1";
		final int port = Integer.parseInt(Configuration.getProperty(Configuration.TCP_PORT));//Configuration自定义全局配置类
		final int firstMessageSize;
		if (args.length == 3) {
			firstMessageSize = Integer.parseInt(args[2]);
		} else {
			firstMessageSize = 100;
		}

		client = new TcpClientMain(host, port, firstMessageSize);
		client.start();
		//等待同步信号,在childHandler.onActive()中发出
		if(!latch.await(30000, TimeUnit.MILLISECONDS)) {//超时
			System.out.println("连接服务器超时！");
			return;
		}
//		System.out.println("主线程：" + Thread.currentThread().getName());
		UserIdentify identifier = (UserIdentify)SpringContextHolder.getBean(UserIdentify.class);
		identifier.login(ctx);

		//输入命令执行相应操作
		System.err.println("try typing in following actions and have fun!\nuser!login name=1&password=123\nuser!logout\nuser!reconnect sessionId=2\n");
		Scanner scanner = new Scanner(System.in);
		int requestId = 1;
		//循环读取命令，并执行，命令格式为：command [param]
		while(true){
			String line = scanner.nextLine(); 
			if (line.equals("exit")) {
				System.out.println("TcpClient exit");
				System.exit(2);
				break; 
			}
			Scanner sc = new Scanner(line);
			String command = sc.next();
			String params = null;
			if (sc.hasNext())
				params = sc.next();
			else
				params = "";
			client.sendMsg(requestId, command, params);
			requestId++;
		}
	}


	//发送一条消息,在main函数中调用
	public void sendMsg(int requestId, String command, String params){
		Request request = new Request(requestId, command, params, "0", ctx);
		ctx.write(request);
		ctx.flush();
		System.err.println("send request:" + requestId + " " + command + " " + params);
	}
	
	
	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		TcpClientMain.ctx = ctx;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public static String getHost() {
		return host;
	}

	public static int getPort() {
		return port;
	}

	public static int getFirstMessageSize() {
		return firstMessageSize;
	}

	public Channel getChannel() {
		return channel;
	}

}
