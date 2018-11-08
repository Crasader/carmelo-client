package carmelo.examples.client.device;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import carmelo.examples.client.device.domain.BitPort;
import carmelo.examples.client.device.domain.Composite;
import carmelo.examples.client.device.domain.DeviceType;
import carmelo.examples.client.device.domain.DeviceUtil;
import carmelo.json.JsonBuilder;

//用于本地device信息的读取和配置操作
@Component
public class DeviceConfig {

	//从文件中解析json数据
	//	private static String classpath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	//	private static String fileName = classpath + "device.json";
	private static String fileName = "/home/lgc/git/carmelo-client/carmelo-client/src/main/resources/" + "device.json";
	private static File file = new File(fileName);
	private static Map<Integer, Composite> compositeMap = new HashMap<Integer, Composite>();
	private static Map<Integer, BitPort> bitportMap = new HashMap<Integer, BitPort>();
	private static int rootId;

	//读取配置文件，并初始化两个Composite, BitPort两个map
	static {

		try {
			parseComposite(getComposite(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//通过递归解析组合,并将识别到的BitPort和Composite放入对应的map中
	public static void parseComposite(JSONObject jsonObject, int level) {
		Composite composite = new Composite();
		composite.setId(jsonObject.getIntValue("id"));
		composite.setName(jsonObject.getString("name"));
		composite.setParentId(jsonObject.getIntValue("parentId"));
		JSONArray array = jsonObject.getJSONArray("children");
		long[] children = new long[array.size()];//用于存放Composite的children
		int index = 0;//用于记录children索引
		Iterator<Object> childrenItr = array.iterator();
		while(childrenItr.hasNext()) {
			JSONObject obj = (JSONObject) childrenItr.next();
			if(obj.getInteger("type") == DeviceType.BITPORT.getType()) {
				//子元素是一个BitPort
				BitPort bp = JSONObject.toJavaObject(obj, BitPort.class);
				bitportMap.put(bp.getId(), bp);
				System.out.println("bp: " + bp.getId() + "," + bp.getName() + "," + bp.getParentId() + "," + bp.isReadable() + "," + bp.isWriteable());
				children[index] = DeviceUtil.intToLong(new int[] {DeviceType.BITPORT.getType(), bp.getId()});
				index++;
			}
			if(obj.getInteger("type") == DeviceType.COMPOSITE.getType()) {
				//子元素是一个Composite
				children[index] = DeviceUtil.intToLong(new int[] {DeviceType.COMPOSITE.getType(), obj.getInteger("id")});
				index++;
				parseComposite(obj, level+1);
			}
		}
		composite.setChildren(children);
		compositeMap.put(composite.getId(), composite);
		System.out.println("cp: " + composite.getId() + "," + composite.getName() 
		+ "," + composite.getParentId() + "," + composite.getChildren());
		if(level == 0) setRootId(composite.getId());//记录根组合的id号

	}


	//利用compositeMap, bitportMap, rootId反向生成本地JSON配置文件
	public static JSONObject convertToJSON() {
		return compositeToJSON( compositeMap.get(rootId));
	}
	//利用compositeMap, bitportMap, rootId反向生成本地JSON配置文件
	public static JsonBuilder convertToJSONBuilder() {
		return compositeToJSONBuilder(null, compositeMap.get(rootId));
	}

	//将composite通过递归转换成JSON串
	private static JSONObject compositeToJSON( Composite composite) {
		JSONObject builder = new JSONObject();
		builder.put("type", 2);
		builder.put("id", composite.getId());
		builder.put("name", composite.getName());
		builder.put("parentId", composite.getParentId());
		JSONArray children = new JSONArray();
		for(long child : composite.getChildren()) {
			int[] index = DeviceUtil.longToInt(child);
			if(index[0] == DeviceType.BITPORT.getType()) {
				BitPort bp = bitportMap.get(index[1]);
				JSONObject bpChild = new JSONObject();
				bpChild.put("type", 1);
				bpChild.put("id", bp.getId());
				bpChild.put("name", bp.getName());
				bpChild.put("parentId", bp.getParentId());
				bpChild.put("readable", bp.isReadable());
				bpChild.put("writeable", bp.isWriteable());
				children.add(bpChild);
			}

			if(index[0] == DeviceType.COMPOSITE.getType()) {
				Composite cp = compositeMap.get(index[1]);
				children.add(compositeToJSON(cp));
			}
		}

		builder.put("children", children);

		return builder;
	}

	//将composite通过递归转换成JSON串
	private static JsonBuilder compositeToJSONBuilder(JsonBuilder builder, Composite composite) {
		if(builder == null) builder = new JsonBuilder();
		builder.startObject();
		builder.writeKey("type");
		builder.writeValue(2);
		builder.writeKey("id");
		builder.writeValue(composite.getId());
		builder.writeKey("name");
		builder.writeObject(composite.getName());
		builder.writeKey("parentId");
		builder.writeValue(composite.getParentId());
		builder.writeKey("children");
		builder.startArray();		

		for(long child : composite.getChildren()) {
			int[] index = DeviceUtil.longToInt(child);
			if(index[0] == DeviceType.BITPORT.getType()) {
				BitPort bp = bitportMap.get(index[1]);
				builder.startObject();
				builder.writeKey("type");
				builder.writeValue(1);
				builder.writeKey("id");
				builder.writeValue(bp.getId());
				builder.writeKey("name");
				builder.writeObject(bp.getName());
				builder.writeKey("parentId");
				builder.writeValue(bp.getParentId());
				builder.writeKey("readable");
				builder.writeValue(bp.isReadable());
				builder.writeKey("writeable");
				builder.writeValue(bp.isWriteable());
				builder.endObject();
			}
			if(index[0] == DeviceType.COMPOSITE.getType()) {
				Composite cp = compositeMap.get(index[1]);
				compositeToJSONBuilder(builder, cp);
			}
			
		}
		
		builder.endArray();
		builder.endObject();
		
		return builder;
	}

	//读取本地device配置信息
	public static JSONObject getComposite() throws IOException {
		InputStream is = new FileInputStream(file);
		byte[]buf = new byte[1024];
		int hasRead = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while((hasRead = is.read(buf))!=-1){ 
			baos.write(buf,0,hasRead);
		}
		is.close();
		return JSON.parseObject(baos.toString());
	}

	//写入本地配置信息
	public static void setComposite(JSONObject composite) throws IOException {
		OutputStream os = new FileOutputStream(file);
		os.write(composite.toJSONString().getBytes());
		os.close();
		return;
	}

	public static Map<Integer, BitPort> getBitPortMap(){
		return bitportMap;
	}

	public static Map<Integer, Composite> getCompositeMap(){
		return compositeMap;
	}

	public static int getRootId() {
		return rootId;
	}

	private static void setRootId(int rootId) {
		DeviceConfig.rootId = rootId;
	}

	public static void refreshId(JSONObject registerInfo) throws IOException {
		Set<String> keys = registerInfo.keySet();
		refreshCompositeId(keys, registerInfo, rootId);
		//检查是否要更新rootId
		if(keys.contains(Integer.toString(rootId))) {
			rootId = registerInfo.getIntValue(Integer.toString(rootId));
		}
		//将device两个map转换为json配置信息并写入配置文件
		OutputStream os = new FileOutputStream(new File("/home/lgc/git/carmelo-client/carmelo-client/src/main/resources/" + "device.json"));
//		JSONObject jsonObject = convertToJSON();
//		os.write(jsonObject.toJSONString().getBytes());
		JsonBuilder jsonBuilder = convertToJSONBuilder();
		os.write(jsonBuilder.toBytes());
		os.flush();
		os.close();
	}

	private static void refreshCompositeId(Set<String> keys, JSONObject registerInfo, int id) {
		Composite composite = compositeMap.get(id);

		//检查composite子成员
		long[] children = composite.getChildren();
		for(int i=0; i<children.length; i++) {
			int[] childDetail = DeviceUtil.longToInt(children[i]);
			if(childDetail[0] == DeviceType.BITPORT.getType()) {
				//子成员是BitPort
				if(keys.contains(Integer.toString(childDetail[1]))){
					BitPort bitPort = bitportMap.get(childDetail[1]);
					//修改bitPort的id
					int newId = registerInfo.getIntValue(Integer.toString(childDetail[1]));
					bitPort.setId(newId);
					//map中更新
					bitportMap.remove(childDetail[1]);
					bitportMap.put(newId, bitPort);
					//修改本级composite的子成员信息
					childDetail[1] = newId;
					children[i] = DeviceUtil.intToLong(childDetail);
				}
			}
			if(childDetail[0] == DeviceType.COMPOSITE.getType()) {
				//子成员是composite
				//递归调用自身
				refreshCompositeId(keys, registerInfo, childDetail[1]);
				if(keys.contains(Integer.toString(childDetail[1]))){
					int newId = registerInfo.getIntValue(Integer.toString(childDetail[1]));
					//修改本级composite的子成员信息
					childDetail[1] = newId;
					children[i] = DeviceUtil.intToLong(childDetail);
				}
			}
		}
		//更新children信息
		composite.setChildren(children);
		//检查composite本身的id是否要更新
		if(keys.contains(Integer.toString(id))) {
			composite.setId(registerInfo.getIntValue(Integer.toString(id)));
			//移除旧的composite
			compositeMap.remove(id);
			compositeMap.put(composite.getId(), composite);
		}else {
			compositeMap.replace(composite.getId(), composite);
		}
	}

}
