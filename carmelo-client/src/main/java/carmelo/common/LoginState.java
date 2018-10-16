package carmelo.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import carmelo.examples.client.business.UserIdentify;
import carmelo.examples.client.sync.FutureManager;
import carmelo.examples.client.sync.RequestId;
import carmelo.examples.client.sync.SyncFuture;
import carmelo.json.ResponseDto;
import carmelo.json.ResponseType;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import io.netty.channel.ChannelHandlerContext;

//全局记录登录状态
public class LoginState {
	//登录状态
	private static boolean isLogin = false;
	//连接sessionId
	private static String sessionId;

	//实现心跳机制
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static ScheduledFuture<?> heartBeat;
	private static ChannelHandlerContext ctx;



	public static boolean isLogin() {
		synchronized(LoginState.class) {
			return isLogin;
		}
	}

	public static void setLoginState(boolean isLogin) {
		synchronized(LoginState.class) {
			LoginState.isLogin = isLogin;
		}
	}

	public static String getSessionId() {
		return sessionId;
	}

	public static void setSessionId(String sessionId) {
		synchronized(LoginState.class) {
			LoginState.sessionId = sessionId;
		}
	}

	public static ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public static void setScheduler(ScheduledExecutorService scheduler) {
		LoginState.scheduler = scheduler;
	}

	public static ScheduledFuture<?> getHeartBeat() {
		return heartBeat;
	}

	public static void setHeartBeat(ScheduledFuture<?> heartBeat) {
		LoginState.heartBeat = heartBeat;
	}


	public static ChannelHandlerContext getCtx() {
		return ctx;
	}

	public static void setCtx(ChannelHandlerContext ctx) {
		LoginState.ctx = ctx;
	}

	public static void setHeartBeatTask(ChannelHandlerContext ctx) {
		//在ClientHandler.channelAcitve()中调用本函数，说明ctx刚刚更新
		LoginState.ctx = ctx;
		//先关闭之前的任务，如果有
    	if (heartBeat != null) {
		    heartBeat.cancel(true);
		    heartBeat = null;
		}
		//开启心跳任务，在客户端登录成功后发送心跳，10秒一次    
		heartBeat = scheduler.scheduleWithFixedDelay(new HeartBeatTask(ctx), 10, 10, TimeUnit.SECONDS);
	}
	
	public static void shutDownHeartBeatTask() {
		//出错关闭心跳，关闭channel
    	if (heartBeat != null) {
		    heartBeat.cancel(true);
		    heartBeat = null;
		}
	}

	//内部私有类，实现心跳
	private static class HeartBeatTask implements Runnable {
		private final ChannelHandlerContext ctx;

		public HeartBeatTask(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		public void run() {
			try {
				System.out.println("heart beat task");
				//检查客户端登录状态，若已经登录，则发送心跳
				if(LoginState.isLogin()) {//已经登录
					int requestId = RequestId.get();
					String command = "heartbeat!heartbeat";
					String params = "sessionId="+ LoginState.getSessionId();
					//在发消息时，Request对象内的sessionId成员并不会发出，所以要放在params里发。
					Request request=new Request(requestId, command, params, LoginState.getSessionId(), ctx);
					//创建并添加future
					SyncFuture<Response> future = FutureManager.getInstance().createFuture(requestId);
					ctx.writeAndFlush(request);
					//获取结果，若返回错误，说明重连超时，重新登录
					Response response = future.get(60*1000, TimeUnit.MILLISECONDS);
					if(response == null) {//超时
						//目前暂时忽略
						isLogin = false;
						sessionId = null;
						return;
					}
					//服务器返回的内容
					byte[] contents = response.getContents();
					//				    System.out.println("返回内容：" + new String(contents));
					ResponseDto responseDto = new ResponseDto(new String(contents));
					int responseType = responseDto.getResponseType();

					if(responseType == ResponseType.FAIL.getType()) {
						//心跳得到失败回复，说明用户已被下线，需要重新进行业务登录连接
						isLogin = false;
						sessionId = null;
						//重新连接后，原有的task会被取消,所以重新登录的任务不能放在这里
						return;
					}else if(responseType == ResponseType.SUCCESS.getType()) {
						//心跳得到成功回复，直接返回
						return;
					}else {
						isLogin = false;
						sessionId = null;
						System.out.println("心跳回复未知错误,请联系开发人员");
						return;
					}
				}else {
					//重新登录
					UserIdentify identifier = (UserIdentify)SpringContextHolder.getBean(UserIdentify.class);
					identifier.login(ctx);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
