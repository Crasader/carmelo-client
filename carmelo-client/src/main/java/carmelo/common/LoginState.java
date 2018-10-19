package carmelo.common;

import io.netty.channel.ChannelHandlerContext;

//全局记录登录状态
public class LoginState {
	//登录状态
	private static boolean isLogin = false;
	//连接sessionId
	private static String sessionId;

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

	public static ChannelHandlerContext getCtx() {
		return ctx;
	}

	public static void setCtx(ChannelHandlerContext ctx) {
		LoginState.ctx = ctx;
	}

}
