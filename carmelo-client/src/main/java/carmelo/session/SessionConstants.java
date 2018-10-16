package carmelo.session;

import io.netty.util.AttributeKey;

/*
 * 自定义工具类
 */
public class SessionConstants {
	
	 public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("session_id");
	 
	 public static final String USER_ID = "user_id";
	 
	 public static final long SESSION_TIME_OUT_TIME = 3 * 60 * 1000;//3分钟超时 3 * 60 * 1000
}
