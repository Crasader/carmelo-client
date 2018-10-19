package carmelo.json;

public enum MessageType {
	
	REQUEST(1),
	
	RESPONSE(2);
	
	
	private int type;
	
	MessageType(int type){
		this.type = type;
	}
	
	public int getType(){
		return this.type;
	}
	
	public static MessageType valueof(int type) {
		switch(type) {
		case 1:
			return REQUEST;
		case 2:
			return RESPONSE;
		default:
			return null;
		}
	}

}
