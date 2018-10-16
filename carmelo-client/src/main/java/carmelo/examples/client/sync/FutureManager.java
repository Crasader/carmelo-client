package carmelo.examples.client.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import carmelo.servlet.Response;

/*
 * 自定义工具类，采用单例模式 ,配合SyncFuture实现请求-响应同步机制
 */
public class FutureManager {
	
	private final static FutureManager instance = new FutureManager();
	
	
	public static FutureManager getInstance(){
		return instance;
	}
	
	private FutureManager(){
		initThread();
	}
	
	//futureMap用来管理当前所有的同步业务
	private Map<Integer, SyncFuture<Response>> futureMap = new ConcurrentHashMap<Integer, SyncFuture<Response>>();

	public SyncFuture<Response> getFuture(Integer requestId){
		return futureMap.get(requestId);
	}
	
	
	private void removeFuture(Integer requestId){
		futureMap.remove(requestId);
	}
	
	//创建一个future,并添加到map中
	public SyncFuture<Response> createFuture(int requestId){
		SyncFuture<Response> future = new SyncFuture<Response>(requestId);
		addFuture(future);
		return future;
	}
	
	public void addFuture(SyncFuture<Response> future ) {
		futureMap.put(future.getRequestId(), future);
	}
	
	public boolean containsFuture(int requestId) {
		if(futureMap.containsKey(requestId)) {
			return true;
		}
		return false;
	}
	
//FutureManager采用单例模式，使用单独的线程来进行future管理
	public void initThread() {
		new Thread() {
			public void run() {
				while (true) {
					//System.out.println("future Manager task");
					for (SyncFuture<Response> future : futureMap.values()) {
						try {
							if (future.isTimeout() || future.isDone())
							{
								removeFuture(future.getRequestId());//从futureMap里面移除
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000 * 60);//1分钟清理一次
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	
}
