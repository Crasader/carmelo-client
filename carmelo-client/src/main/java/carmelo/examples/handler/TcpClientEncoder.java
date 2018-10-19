package carmelo.examples.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import carmelo.json.MessageType;
import carmelo.servlet.OutputMessage;
import carmelo.servlet.Request;
import carmelo.servlet.Response;

////编码器
//public class TcpClientEncoder extends MessageToByteEncoder<Request> {
//
//    @Override
//    protected void encode(ChannelHandlerContext ctx, Request request, ByteBuf out) throws Exception {
//        
//    	// encoding format: totalLength + requestId + commandLength + command + params
//        int requestId = request.getId();
//		String command = request.getCommand();
//		String params = request.getParams();
//		int commandLength = command.length();
//		int totalLength = 8 + commandLength + params.length();
//        out.writeInt(totalLength);
//        out.writeInt(requestId);
//        out.writeInt(commandLength);
//        out.writeBytes(command.getBytes());
//        out.writeBytes(params.getBytes());
//    }
//}


//编码器
public class TcpClientEncoder extends MessageToByteEncoder<OutputMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, OutputMessage message, ByteBuf out) throws Exception {

		//消息类型，可以是REQUEST或RESPONSE
		MessageType messageType = message.getMessageType();
		//消息格式：
		//如果是REQUEST: messageType + totalLength + requestId + commandLength + command + params
		//如果是RESPONSE： messageType + totalLength + requestId + contents
		out.writeInt(messageType.getType());
		int totalLength;
		
		switch(messageType) {		
		case REQUEST:
			Request request = message.getRequest();
			// encoding format: totalLength + requestId + commandLength + command + params
			int requestId = request.getId();
			String command = request.getCommand();
			String params = request.getParams();
			int commandLength = command.length();
			totalLength = 8 + commandLength + params.length();
			out.writeInt(totalLength);
			out.writeInt(requestId);
			out.writeInt(commandLength);
			out.writeBytes(command.getBytes());
			out.writeBytes(params.getBytes());
			break;

		case RESPONSE:
			//如果是RESPONSE： messageType + totalLength + requestId + contents
			Response response = message.getResponse();
			int responseId = response.getId();
	        byte[] responseContents = response.getContents();
	        totalLength = 4 + responseContents.length;
	    	out.writeInt(totalLength);
	    	out.writeInt(responseId);
	        out.writeBytes(responseContents);
			break;
			
		default:
			break;
			
		}
		
	}
}
