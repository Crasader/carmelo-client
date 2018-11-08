package carmelo.examples.client.device.domain;

public class Composite implements IPort {
	
	private int id;
	private String name;
	private int parentId;
	private long[] children;
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long[] getChildren() {
		return children;
	}
	
	public void setChildren(long[] children) {
		this.children = children;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
    
	

}
