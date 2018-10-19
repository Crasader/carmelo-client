package carmelo.examples.handler;

import java.util.List;

import carmelo.json.MessageType;
import carmelo.servlet.Request;
import carmelo.servlet.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;



//解码器
public class TcpClientDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		// wait until the length prefix is available
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();

//		System.out.println("decoder 收到一条消息");
		//读取消息类型，是Request还是Response
		MessageType messageType = MessageType.valueof(in.readInt());
		int totalLength;
		int requestId;
		switch(messageType) {

		case RESPONSE://来自服务端的反馈，可能是某个请求的响应，也可能是主动的消息推送
			//消息格式：messageType + totalLength + requestId + contents
			totalLength = in.readInt();

			if (in.readableBytes() < totalLength) {
				in.resetReaderIndex();
				return;
			}

			requestId = in.readInt();
			byte[] contentBytes = new byte[totalLength - 4];
			in.readBytes(contentBytes);

			Response response = new Response(requestId, contentBytes);
			//					System.err.println("receive reponse:" + requestId + " " + new String(contentBytes));
			System.out.println("Response totalLength:" + totalLength + " requesId:" + requestId + " contentBytes:" + new String(contentBytes));
			out.add(response);			
			break;

		case REQUEST://来自服务端的请求
			// encoding format: messageType + totalLength + requestId + commandLength + command + params
			totalLength = in.readInt();
			if (in.readableBytes() < totalLength) {
				in.resetReaderIndex();
				return;
			}

			requestId = in.readInt();
			int commandLength = in.readInt();
			byte[] commandBytes = new byte[commandLength];
			byte[] paramsBytes = new byte[totalLength - 8 - commandLength];
			in.readBytes(commandBytes);
			in.readBytes(paramsBytes);
			String command = new String(commandBytes);
			String params = new String(paramsBytes);
			Request request = new Request(requestId, command, params, "0", ctx);
			System.out.println("来自服务端的请求：" + requestId + " " + command + " " + params);
			out.add(request);
			break;			
			
		default:
			break;

		}
	}
}




////解码器
//public class TcpClientDecoder extends ByteToMessageDecoder {
//
//	@Override
//	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
//			List<Object> out) {
//		// wait until the length prefix is available
//		if (in.readableBytes() < 4) {
//			return;
//		}
//		in.markReaderIndex();
//
//		// encoding format: totalLength + requestId + responseContent
//		int totalLength = in.readInt();
//		if (in.readableBytes() < totalLength) {
//			in.resetReaderIndex();
//			return;
//		}
//
//		int requestId = in.readInt();
//		int contentLength = totalLength - 4;
//		byte[] contentBytes = new byte[contentLength];
//		in.readBytes(contentBytes);
//		Response response = new Response(requestId, contentBytes);
////		System.err.println("receive reponse:" + requestId + " " + new String(contentBytes));
//		System.out.println("totalLength:" + totalLength + " requesId:" + requestId + " contentBytes:" + new String(contentBytes));
//		out.add(response);
//	}
//}
