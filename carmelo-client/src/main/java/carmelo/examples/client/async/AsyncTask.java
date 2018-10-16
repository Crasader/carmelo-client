package carmelo.examples.client.async;

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

//使用spring 的异步注解实现异步
//https://www.jb51.net/article/109345.htm
@Component
public class AsyncTask {

	@Async//("asyncExecutor")
	public Future<String> asyncMethodWithReturnType() {
		System.out.println("Execute method asynchronously - "
				+ Thread.currentThread().getName()); 
		try { 
			Thread.sleep(5000); 
			System.out.println("异步结果：hello world !!!!");
			return new AsyncResult<String>("hello world !!!!"); 
		} catch (InterruptedException e) { 
			// 
		} 

		return null; 
	}

}
