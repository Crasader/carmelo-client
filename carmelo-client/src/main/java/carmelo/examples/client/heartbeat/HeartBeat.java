package carmelo.examples.client.heartbeat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import carmelo.common.LoginState;
import carmelo.common.SpringContext;
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

@Component
public class HeartBeat {

	@Scheduled(cron = "0/60 * * * * ?")  //每10秒执行一次
	public void heartBeat(){
		ChannelHandlerContext ctx = LoginState.getCtx();
		if(ctx == null) {
			return;
		}
		if( ctx.channel().isActive() && LoginState.isLogin()) {//已经登录
//			System.out.println("heart beat: Loged in");

			int requestId = RequestId.get();
			String command = "heartbeat!heartbeat";
			String params = "sessionId="+ LoginState.getSessionId();
			//在发消息时，Request对象内的sessionId成员并不会发出，所以要放在params里发。
			Request request=new Request(requestId, command, params, LoginState.getSessionId(), ctx);
			//创建并添加future
			FutureManager fm = (FutureManager)SpringContext.getBean(FutureManager.class);
			SyncFuture<Response> future = fm.createFuture(requestId);
			ctx.writeAndFlush(new OutputMessage(MessageType.REQUEST, request));
			//获取结果，若返回错误，说明重连超时，重新登录
			Response response;
			try {
				response = future.get(60*1000, TimeUnit.MILLISECONDS);
				if(response == null) {//超时
					//目前暂时忽略
					LoginState.setLoginState(false);
					LoginState.setSessionId(null);
					return;
				}
				//服务器返回的内容
				byte[] contents = response.getContents();
//				System.out.println("返回内容：" + new String(contents));
				ResponseDto responseDto = new ResponseDto(new String(contents));
				int responseType = responseDto.getResponseType();

				if(responseType == ResponseType.FAIL.getType()) {
					//心跳得到失败回复，说明用户已被下线，需要重新进行业务登录连接
					LoginState.setLoginState(false);
					LoginState.setSessionId(null);
					//在TcpClientHandler.inActive()里掉线会自动重连
					return;
				}else if(responseType == ResponseType.SUCCESS.getType()) {
					//心跳得到成功回复，直接返回
					return;
				}else {
					LoginState.setLoginState(false);
					LoginState.setSessionId(null);
					System.out.println("心跳回复未知错误,请联系开发人员");
					return;
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}



//CRON表达式    含义 
//
//"0 0 12 * * ?"    每天中午十二点触发 
//
//"0 15 10 ? * *"    每天早上10：15触发 
//
//"0 15 10 * * ?"    每天早上10：15触发 
//
//"0 15 10 * * ? *"    每天早上10：15触发 
//
//"0 15 10 * * ? 2005"    2005年的每天早上10：15触发 
//
//"0 * 14 * * ?"    每天从下午2点开始到2点59分每分钟一次触发 
//
//"0 0/5 14 * * ?"    每天从下午2点开始到2：55分结束每5分钟一次触发 
//
//"0 0/5 14,18 * * ?"    每天的下午2点至2：55和6点至6点55分两个时间段内每5分钟一次触发 
//
//"0 0-5 14 * * ?"    每天14:00至14:05每分钟一次触发 
//
//"0 10,44 14 ? 3 WED"    三月的每周三的14：10和14：44触发 
//
//"0 15 10 ? * MON-FRI"    每个周一、周二、周三、周四、周五的10：15触发 