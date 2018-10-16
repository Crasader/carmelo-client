package carmelo.examples.client.business;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import carmelo.common.LoginState;
import carmelo.common.UserConfiguration;
import carmelo.examples.client.sync.FutureManager;
import carmelo.examples.client.sync.RequestId;
import carmelo.examples.client.sync.SyncFuture;
import carmelo.json.ResponseDto;
import carmelo.json.ResponseType;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import io.netty.channel.ChannelHandlerContext;

//用户的注册与认证
//每一个业务类包含一个ctx变量，一个requestId,由调用它的地方传入，在业务类内部完成消息的发送

@Component
public class UserIdentify {

	private static ChannelHandlerContext ctx;

	private static int requestId;

//	private static UserIdentify userIdentify;


	//使用Spring, 默认单例模式
	public UserIdentify() {

	}

//	public static UserIdentify getUserIdentifier(ChannelHandlerContext ctx) {
//		if(userIdentify == null) {
//			userIdentify = new UserIdentify();
//		}
//		//掉线重连后，ctx需要更新
//		userIdentify.setChannelContext(ctx);
//		return userIdentify;
//	}

	//登录服务器
	@Async//异步执行
	public void login(ChannelHandlerContext ctx) throws InterruptedException, ExecutionException, TimeoutException {
//		System.out.println("登录线程：" + Thread.currentThread().getName());
		this.setChannelContext(ctx);
		String name = UserConfiguration.getProp("name");
		String password = UserConfiguration.getProp("password");
		if(name==null || password==null) {
			System.out.println("用户信息未配置，申请注册新用户！");
			//用户信息未配置，申请注册新用户
			apply(ctx);
			return;
		}
		String params = "name=" + name + "&password=" + password;
		requestId = RequestId.get();
		Request request = new Request(requestId, "user!login", params, "0", ctx);
		//创建并添加future
		SyncFuture<Response> future = FutureManager.getInstance().createFuture(requestId);
		//发送请求
		ctx.write(request);
		ctx.flush();
		//同步等待返回结果
		Response response = future.get(3000, TimeUnit.MILLISECONDS);
		//返回格式
		if(response == null) {
			System.out.println("登录服务器超时,3秒后重试！");
			Thread.sleep(3000);
			login(ctx);
			return;
		}
		//服务器返回的内容
		byte[] contents = response.getContents();
		//System.out.println("返回内容：" + new String(contents));
		ResponseDto responseDto = new ResponseDto(new String(contents));
		int responseType = responseDto.getResponseType();
		//		System.out.println("login返回类型:" + responseType + " 失败:" + ResponseType.FAIL.getType() + " 成功:" + ResponseType.SUCCESS.getType());
		if(responseType == ResponseType.FAIL.getType()) {
			String content = (String)responseDto.getData();
			if(content.equals("already loged in")) {//已经登录
				System.out.println("用户"+ name + "已经登录！");
				LoginState.setLoginState(true);
				return;
				//登录失败，申请注册新用户
			}else {
				System.out.println("登录失败，申请注册新用户。" + (String)responseDto.getData());
				apply(ctx);
				return;
			}
		}else if(responseType == ResponseType.SUCCESS.getType()) {
			JSONObject jsonObject = JSON.parseObject(responseDto.getData().toString());
			String sessionId = jsonObject.getString("sessionId");//获取到sessionId,暂时不做什么处理
			LoginState.setSessionId(sessionId);
			LoginState.setLoginState(true);//设置全局登录成功
			System.out.println("登录成功，sessionId:" + sessionId);
			return;
		}else {
			System.out.println("未知错误，登录失败,请联系开发人员");
			return;
		}
	}

	//向服务器申请注册新用户
	@Async
	public void apply(ChannelHandlerContext ctx) throws InterruptedException, ExecutionException, TimeoutException {
		this.setChannelContext(ctx);
		//获取一个请求id
		int requestId = RequestId.get();
		//要发送的请求
		Request request = new Request(requestId, "user!apply", "", "0", ctx);

		//创建并添加一个future
		SyncFuture<Response> future = FutureManager.getInstance().createFuture(requestId);
		//发送请求
		ctx.write(request);
		ctx.flush();
		//同步等待结果
		Response response = future.get(3000, TimeUnit.MILLISECONDS);
		if(response == null) {
			System.out.println("申请注册新用户连接超时，申请失败！");
			return;
		}
		//服务器返回的内容
		byte[] contents = response.getContents();
		System.out.println("返回内容：" + new String(contents));
		ResponseDto responseDto = new ResponseDto(new String(contents));
		int responseType = responseDto.getResponseType();
		System.out.println("apply返回类型:" + responseType + " 失败:" + ResponseType.FAIL.getType() + " 成功:" + ResponseType.SUCCESS.getType());
		if(responseType == ResponseType.FAIL.getType()) {
			System.out.println((String)responseDto.getData());
			return;
		}else if(responseType == ResponseType.SUCCESS.getType()) {
			JSONObject jsonObject = JSON.parseObject(responseDto.getData().toString());
			//服务器端只在login和apply方法中生成sessionId
			String sessionId = jsonObject.getString("sessionId");//获取到sessionId
			String name = jsonObject.getString("name");//获取用户名
			String password = jsonObject.getString("password");//获取密码
			LoginState.setSessionId(sessionId);//保存sessionId
			LoginState.setLoginState(true);//设置全局登录成功
			UserConfiguration.setProp("name", name);//保存用户名
			UserConfiguration.setProp("password", password);//保存用户密码
			System.out.println("申请注册并登录成功，sessionId:" + sessionId);
			return;
		}else {
			System.out.println("未知错误，申请注册失败，请联系开发人员");
			return;
		}
	}

	private void setChannelContext(ChannelHandlerContext ctx) {
		UserIdentify.ctx = ctx;
	}

	public ChannelHandlerContext getChannelContext() {
		return ctx;
	}

}
