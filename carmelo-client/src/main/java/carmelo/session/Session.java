package carmelo.session;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import carmelo.servlet.Response;
import io.netty.channel.Channel;


/*
 * 自定义客户端连接管理类,由SessionManager管理生成
 * Session管理的是与客户端之间的连接
 * 
 */
public class Session {
	
	private String sessionId;
	
	private Map<String, Object> params = new ConcurrentHashMap<String, Object>();
	
	private long lastAccessTime = System.currentTimeMillis();
	
	private Channel channel;

	public Session(String sessionId){
		this.sessionId = sessionId;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public boolean isTimeout() {
		return System.currentTimeMillis() - this.lastAccessTime >= SessionConstants.SESSION_TIME_OUT_TIME;
	}
	
	public void access() {
		this.lastAccessTime = System.currentTimeMillis();
	}

	public void push(Response response) {
		channel.write(response);
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
}
