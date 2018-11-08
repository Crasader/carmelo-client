package carmelo.examples.client.business;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import carmelo.common.LoginState;
import carmelo.common.SpringContext;
import carmelo.examples.client.device.DeviceConfig;
import carmelo.examples.client.sync.FutureManager;
import carmelo.examples.client.sync.RequestId;
import carmelo.examples.client.sync.SyncFuture;
import carmelo.json.MessageType;
import carmelo.json.ResponseDto;
import carmelo.json.ResponseType;
import carmelo.servlet.OutputMessage;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import io.netty.channel.ChannelHandlerContext;

//负责本地device在服务端的上线，注册，更新等工作
@Component
public class DeviceBusiness {
	private static final Logger logger = LoggerFactory.getLogger(DeviceBusiness.class);
	private static ChannelHandlerContext ctx;
	private static String session = LoginState.getSessionId();

	//本地device上线
	@Async
	public static void getOnline() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		//检查登录状态
		if(!LoginState.isLogin()) {
			logger.error("Not logged in, can't get device online!");
			return;
		}

		String params = "composite=" + DeviceConfig.getComposite().toJSONString();//格式如"key1=value1&key2=value2.."
		int requestId = RequestId.get();
		Request request = new Request(requestId, "device!getOnline", params, session, ctx);
		//创建并添加future
		FutureManager fm = (FutureManager)SpringContext.getBean(FutureManager.class);
		SyncFuture<Response> future = fm.createFuture(requestId);
		//发送请求
		ctx = LoginState.getCtx();
		ctx.write(new OutputMessage(MessageType.REQUEST, request));
		ctx.flush();
		//同步等待返回结果
		Response response = future.get(3000, TimeUnit.MILLISECONDS);

		//返回格式
		if(response == null) {
			logger.error("设备上线超时,3秒后重试！");
			Thread.sleep(3000);
			getOnline();
			return;
		}
		//服务器返回的内容
		byte[] contents = response.getContents();
		//System.out.println("返回内容：" + new String(contents));
		ResponseDto responseDto = new ResponseDto(new String(contents));
		int responseType = responseDto.getResponseType();
		//		System.out.println("login返回类型:" + responseType + " 失败:" + ResponseType.FAIL.getType() + " 成功:" + ResponseType.SUCCESS.getType());
		if(responseType == ResponseType.FAIL.getType()) {
			logger.error("设备上线失败，请联系开发人员");
		}else if(responseType == ResponseType.SUCCESS.getType()) {
			//解析返回结果
			JSONObject jsonObject = JSON.parseObject(responseDto.getData().toString());
			JSONObject registerInfo = jsonObject.getJSONObject("registerInfo");
			DeviceConfig.refreshId(registerInfo);
			//待完成
			logger.info("返回" + jsonObject.toJSONString());
			return;
		}else {
			logger.error("未知错误，登录失败,请联系开发人员");
			return;
		}
	}
	
	

}
