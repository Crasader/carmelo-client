package carmelo.servlet;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import carmelo.session.Session;

/**
 * an input to an action invocation  
 * 
 * @author needmorecode
 * 包装一个来自客户端的请求，在解码器中解码得到，并将在TcpServerHandler中分发处理
 *
 */
public class Request {
	
	private int id;
	
	private String protocol;
	
	private String command;
	
	private Map<String, String> paramMap;
	
	private String params;
	
	private String sessionId;
	
	private ChannelHandlerContext ctx;
	
	public Request(){
		
	}
	
	public Request(int requestId, String command, String params, String sessionId, ChannelHandlerContext ctx){
		this.setId(requestId);
		this.command = command;
		this.params = params;
		this.paramMap = new HashMap<String, String>();
		if (params.length() > 0){
			for(String param : params.split("&")){//参数用&分隔param1=data1&para2=data2&...
				String[] paramArray = param.split("=");
				paramMap.put(paramArray[0], paramArray[1]);
			}
		}
		this.sessionId = sessionId;
		this.ctx = ctx;
	}



	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, String> paramMap) {
		this.paramMap = paramMap;
	}



	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	
	

}
