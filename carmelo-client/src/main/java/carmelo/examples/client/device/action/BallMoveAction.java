package carmelo.examples.client.device.action;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.hibernate.mapping.Map;
import org.springframework.stereotype.Component;

import carmelo.common.SpringContext;
import carmelo.examples.client.device.DeviceConfig;
import carmelo.examples.client.device.domain.BitPort;
import carmelo.examples.client.environment.World;
import carmelo.json.JsonBuilder;
import carmelo.json.JsonUtil;
import carmelo.servlet.annotation.PassParameter;


@Component
public class BallMoveAction {

	public byte[] stroll(@PassParameter(name = "id")int id){

		World world = (World)SpringContext.getBean(World.class);
		HashMap<Integer, BitPort> bpMap = (HashMap<Integer, BitPort>) DeviceConfig.getBitPortMap();

		BitPort bp = bpMap.get(id);
		String name = bp.getName();
		int action = 0;
		switch(name) {
		case "direction":
			action = KeyEvent.VK_DOWN;
			break;
		case "move":
			action = KeyEvent.VK_UP;
			break;
		default:
			break;
		}
		world.move(action);

		return null;
		//		JsonBuilder builder = JsonUtil.initResponseJsonBuilder();
		//		builder.startObject();
		//		builder.writeKey("result");
		//		builder.writeValue("Hello " + name);
		//		builder.endObject();
		//		builder.endObject();
		//		return builder.toBytes();
	}

}
