package carmelo.examples.client.device.domain;

public enum DeviceType {
	
	BITPORT(1),

	COMPOSITE(2);

	private int type;

	DeviceType(int type){
		this.type = type;
	}

	public int getType(){
		return this.type;
	}

	public static DeviceType valueof(int type) {
		switch(type) {
		case 1:
			return BITPORT;
		case 2:
			return COMPOSITE;
		default:
			return null;
		}
	}


}
