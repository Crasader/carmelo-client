package carmelo.examples.client.sync;


//生成自增加的requestId,使用同步互斥
public class RequestId {
	private static int requestId=0;
	
	public static int get() {
		synchronized(RequestId.class) {
			return requestId++;
		}
	}
}
