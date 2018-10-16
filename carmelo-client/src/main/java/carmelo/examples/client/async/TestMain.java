package carmelo.examples.client.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.ContextConfiguration;

import carmelo.common.SpringContextHolder;
import carmelo.examples.client.business.UserIdentify;



public class TestMain {

	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		BeanFactory beanFactory = new ClassPathXmlApplicationContext("applicationContext.xml");
		
		AsyncTask asyncTask = beanFactory.getBean(AsyncTask.class);
		System.out.println("主线程Invoking an asynchronous method. "
				+ Thread.currentThread().getName()); 
		System.out.println("主线程调用异步任务");
		Future<String> future = asyncTask.asyncMethodWithReturnType();
		System.out.println("主线程继续执行");
		

		UserIdentify userIdentifier = (UserIdentify)SpringContextHolder.getBean(UserIdentify.class);
//		userIdentifier.login();
	}

}
