package carmelo.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

/*
 * Response的javaBean类，用于将json字符串转换为java对象
 * 用法：ResponseDto responseDto = JSON.parseObject(new String(contents), new TypeReference<ResponseDto>() {});
 * 需要将json字符串转换为JSON对象时用法如下：
 * JSONObject jsonObject = JSON.parseObject(JSON_OBJ_STR);
 */
public class ResponseDto {

	/**
	 * response type
	 */
	private int responseType;

	/**
	 * response message
	 */
	private Object data;

	public ResponseDto(){

	}

	public ResponseDto(String JSONString){
		JSONObject jsonObject = JSON.parseObject(JSONString);
		responseType = jsonObject.getIntValue("responseType");
		data = jsonObject.get("data");
	}

	public ResponseDto(int responseType, Object dataDto){
		this.responseType = responseType;
		this.data = dataDto;
	}

	public int getResponseType() {
		return responseType;
	}

	public void setResponeType(int responseType) {
		this.responseType = responseType;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object dataDto) {
		this.data = dataDto;
	}

	//静态方法，用于把JSON字符串转换为ResponseDto对象 
	//用JSON.parseObject(new String(contents), new TypeReference<ResponseDto>() {});总是不能正确解析，原因不明
	public ResponseDto parseObject(String JSONString) {
		JSONObject jsonObject = JSON.parseObject(JSONString);
		responseType = jsonObject.getIntValue("responseType");
		data = jsonObject.get("data");
		return this;
	}

}
