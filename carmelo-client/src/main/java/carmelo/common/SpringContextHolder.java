package carmelo.common;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import carmelo.examples.client.business.UserIdentify;


//spring容器
//启用了异步@Async, 注解扫描
public class SpringContextHolder {
	
	private static BeanFactory beanFactory = new ClassPathXmlApplicationContext("applicationContext.xml");
	
	private SpringContextHolder() {
		
	}

	public static BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	@SuppressWarnings("unchecked")
	public static Object getBean(@SuppressWarnings("rawtypes") Class clazz) {
		return beanFactory.getBean(clazz);
	}
}
