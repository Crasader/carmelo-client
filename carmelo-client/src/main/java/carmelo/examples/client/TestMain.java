package carmelo.examples.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import carmelo.examples.client.device.DeviceConfig;
import carmelo.examples.client.device.domain.BitPort;
import carmelo.examples.client.device.domain.Composite;
import carmelo.examples.client.device.domain.DeviceUtil;
import carmelo.json.JsonBuilder;

public class TestMain {

	private static final Logger logger = LoggerFactory.getLogger(TestMain.class);
	
	public static void main(String[] args) throws Exception {
//		System.out.println(builder.toString());
		//下一步工作：客户端登录后，将本地配置解析成map,发服务端 ，服务端确认后返回成功或更新后的id map
		//客户端更新id map并重新生成配置json文件在本地存储
		logger.error("log test");
		logger.info(DeviceConfig.getBitPortMap().toString());
		
	}


	//fastJSON使用方法
	public void fastJSON() {
		//1. 实体类或集合转JSON串
		BitPort bp1 = new BitPort();

		String jsonStr = JSONObject.toJSONString(bp1);

		//System.out.println(jsonStr);

		//2.JSON串转JSONObject
		JSONObject jObj1 = JSONObject.parseObject(jsonStr);//有多种方式
		jObj1 = (JSONObject) JSONObject.toJSON(bp1);//有多种方式

		//3.JSONObject转实体类
		bp1 = JSONObject.toJavaObject(jObj1, BitPort.class);
		System.out.println("实体类转json串：\n" + JSONObject.toJSONString(bp1));
		//4.JSON串转带泛型的List的集合
		//List<实体类或其他泛型> list = JSON.parseObject(json, new TypeReference<List<实体类或其他泛型>>(){});

		JSONArray jsonArray = new JSONArray();
		jsonArray.add(bp1);
		jsonArray.add(bp1);
		List<BitPort> list = JSON.parseArray(jsonArray.toJSONString(), BitPort.class);
		System.out.println("\nJSON String to List<Object>:");
		for(BitPort bp : list) {
			System.out.println(JSONObject.toJSONString(bp));
		}


		//从文件中解析json数据
		String classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String fileName = classpath + "device.json";//   "/home/lgc/Desktop/" + "device.json";
		InputStream is;
		try {
			File file = new File(fileName);
			is = new FileInputStream(file);
			byte[]buf = new byte[1024];
			int hasRead = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String jsonString;
			while((hasRead = is.read(buf))!=-1){ 
				baos.write(buf,0,hasRead);
			}
			is.close();
			jsonString = baos.toString();
			jsonArray = JSONArray.parseArray(jsonString);
			jsonArray.add(bp1);
			System.out.println("\n从文件读取jsonArray：");
			System.out.println(jsonArray.toJSONString());

			OutputStream os = new FileOutputStream(file);
			os.write(jsonArray.toJSONString().getBytes());
			os.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	//从配置文件读取device配置
	public static void readDeviceConfiguration() throws Exception {
		
		Map<Integer, Composite> compositeMap = DeviceConfig.getCompositeMap();
		System.out.println("\nComposite列表：");
		for(Composite cp : compositeMap.values()) {
			System.out.println(JSON.toJSONString(cp));
			long[] children = cp.getChildren();
			for(long child : children) {
				int[] cf = DeviceUtil.longToInt(child);
				System.out.println("id: " + cf[1]);
			}
		}
		
		System.out.println("\n根Composite:");
		int rootId = DeviceConfig.getRootId();
		Composite rootComposite = compositeMap.get(rootId);
		System.out.println(JSON.toJSONString(rootComposite));
		
		
		Map<Integer, BitPort> bitportMap = DeviceConfig.getBitPortMap();
		System.out.println("\nBitPort列表：");
		for(BitPort bp : bitportMap.values()) {
			System.out.println(JSON.toJSONString(bp));
		}
	}

}


