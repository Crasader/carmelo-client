package carmelo.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/*
 * 用户配置信息，包含username和password使用单独的配置文件，在启动时自动加载。
 * 设计工作模式为：客户端启动时读取本地用户信息，并向服务器登录，若本地没有信息或登录失败，则服务器自动生成一个用户信息，
 * 客户端自动保存该信息，下次启动以该用户身份登录
 */

public class UserConfiguration {
	private static Properties userProp;
	private static String fileName;
	
	
	static {
		String classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
//        fileName = classpath + "user.properties";
        fileName = "/home/lgc/git/carmelo-client/carmelo-client/src/main/resources/" + "user.properties";
        userProp = new Properties();
        InputStream is;
		try {
			is = new FileInputStream(new File(fileName));
			userProp.load(is);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getProp(String key) {
		return userProp.getProperty(key);
	}
	
	public static void setProp(String key, String value) {
		userProp.setProperty(key, value);
		OutputStream os;
		try {
			os = new FileOutputStream(new File(fileName));
			userProp.store(os, "user config");
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
