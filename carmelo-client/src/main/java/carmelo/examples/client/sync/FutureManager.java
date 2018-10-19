package carmelo.examples.client.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import carmelo.servlet.Response;

/*
 * 自定义工具类，采用单例模式 ,配合SyncFuture实现请求-响应同步机制
 */
@Component
public class FutureManager {

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

	@Scheduled(cron = "0/5 * * * * ?")  //每5秒执行一次,清理已经超时完成的future
	public void cleanFutureMap() {
//		System.out.println("future Manager task");
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

	}

}
