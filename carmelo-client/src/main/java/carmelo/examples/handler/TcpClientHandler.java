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
package carmelo.examples.handler;

import java.util.concurrent.TimeUnit;

import carmelo.common.LoginState;
import carmelo.common.SpringContext;
import carmelo.examples.client.TcpClientMain;
import carmelo.examples.client.sync.FutureManager;
import carmelo.examples.client.sync.SyncFuture;
import carmelo.json.MessageType;
import carmelo.json.ResponseDto;
import carmelo.json.ResponseType;
import carmelo.servlet.OutputMessage;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import carmelo.servlet.Servlet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;


//业务handler
public class TcpClientHandler extends ChannelInboundHandlerAdapter {


	private TcpClientMain tcpClient;
	
	private Servlet servlet;
	

	/**
	 * Creates a client-side handler.
	 */
	public TcpClientHandler(int firstMessageSize, TcpClientMain tcpClient) {
		if (firstMessageSize <= 0) {
			throw new IllegalArgumentException("firstMessageSize: " + firstMessageSize);
		}
		this.tcpClient = tcpClient;
		this.servlet = TcpClientMain.getServlet();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		tcpClient.setCtx(ctx);
		//连接建立，ctx可用了，向主线程发同步信号
		tcpClient.getLatch().countDown();
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {		
		
		if(msg instanceof Response) {
			Response response = (Response)msg;

			//如果是futureMap中同步等待的response,则把response传递给future,由相应的业务类处理
			int requestId = response.getId();
			FutureManager fm = (FutureManager)SpringContext.getBean(FutureManager.class);
			if(fm.containsFuture(requestId)) {
				SyncFuture<Response> future = fm.getFuture(requestId);
				future.setResponse(response);//这里会通知发出Request的业务类读取数据并处理
				return;
			}
			return;
		}
		if(msg instanceof Request) {
			//来自服务端的请求（主动推送的消息也以请求的方式发出，但在发出方并不等待Response）
			Response response = servlet.service((Request)msg);
    		if(response != null) {//有反馈内容
    			ctx.writeAndFlush(new OutputMessage(MessageType.RESPONSE, response));
    		}
			return;

		}
		
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.close();
	}



	//实现掉线重连功能 
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		System.err.println("掉线了...");
		//使用过程中断线重连
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			@Override
			public void run() {
				tcpClient.connect(TcpClientMain.getHost(), TcpClientMain.getPort());
			}
		}, 1L, TimeUnit.SECONDS);

		super.channelInactive(ctx);
	}


}
