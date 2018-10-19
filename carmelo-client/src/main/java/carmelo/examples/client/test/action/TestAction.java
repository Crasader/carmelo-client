package carmelo.examples.client.test.action;

import org.springframework.stereotype.Component;

import carmelo.json.JsonBuilder;
import carmelo.json.JsonUtil;
import carmelo.servlet.annotation.PassParameter;


@Component
public class TestAction {
	
	public byte[] sayHello(@PassParameter(name = "name")String name){
		JsonBuilder builder = JsonUtil.initResponseJsonBuilder();
		builder.startObject();
		builder.writeKey("result");
		builder.writeValue("Hello " + name);
		builder.endObject();
		builder.endObject();
		return builder.toBytes();
	}

}
