package carmelo.servlet;

import carmelo.json.MessageType;

public class OutputMessage {
	
	private MessageType messageType;
	
	private Request request;
	
	private Response response;
	
	public OutputMessage(MessageType messageType, Request request) {
		this.messageType = messageType;
		this.request = request;
	}
	
	public OutputMessage(MessageType messageType, Response response) {
		this.messageType = messageType;
		this.response = response;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
	
	

}
