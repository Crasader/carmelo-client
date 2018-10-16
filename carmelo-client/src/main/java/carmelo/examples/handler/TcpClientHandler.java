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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import carmelo.common.LoginState;
import carmelo.examples.client.TcpClientMain;
import carmelo.examples.client.sync.FutureManager;
import carmelo.examples.client.sync.SyncFuture;
import carmelo.json.ResponseDto;
import carmelo.json.ResponseType;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;


//业务handler
public class TcpClientHandler extends ChannelInboundHandlerAdapter {


	private TcpClientMain tcpClient;

	//实现心跳机制
	private static ScheduledExecutorService scheduler;
	private static ScheduledFuture<?> heartBeat;
	/**
	 * Creates a client-side handler.
	 */
	public TcpClientHandler(int firstMessageSize, TcpClientMain tcpClient) {
		if (firstMessageSize <= 0) {
			throw new IllegalArgumentException("firstMessageSize: " + firstMessageSize);
		}
		this.tcpClient = tcpClient;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		tcpClient.setCtx(ctx);
		//连接建立，ctx可用了，向主线程发同步信号
		tcpClient.getLatch().countDown();
		//开启心跳
		LoginState.setHeartBeatTask(ctx);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {		
			
		Response response = (Response)msg;

		//如果是futureMap中同步等待的response,则把response传递给future,由相应的业务类处理
		int requestId = response.getId();
		FutureManager fm = FutureManager.getInstance();
		if(fm.containsFuture(requestId)) {
			SyncFuture<Response> future = fm.getFuture(requestId);
			future.setResponse(response);//这里会通知发出Request的业务类读取数据并处理
			return;
		}
		
		
		//如果是由服务器主动发起的消息推送，交由相应的业务类处理
		byte[] contents = response.getContents();
		//System.out.println("返回内容：" + new String(contents));
		ResponseDto responseDto = new ResponseDto(new String(contents));
		int responseType = responseDto.getResponseType();
		if(responseType == ResponseType.PUSH.getType()) {
			//调用相应业务处理函数
			//方便起见，服务器主动发起的数据推送，在Response的contents中responseDto.data中包装放入一个Request，
			//采用与服务器端业务分发一样的机制，提高代码重用
			Request request = (Request)responseDto.getData();
			
			//servlet.service(request);
			
			return;
		}
		
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LoginState.shutDownHeartBeatTask();
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
