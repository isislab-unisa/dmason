package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Param 
{
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String type;
	@XStreamAsAttribute
	private int runs;
	@XStreamAsAttribute
	private String mode;
	
	
	public Param(String name, String type, int runs, String mode) {
		super();
		this.name = name;
		this.type = type;
		this.runs = runs;
		this.mode = mode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getRuns() {
		return runs;
	}
	public void setRuns(int runs) {
		this.runs = runs;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return  name;//"Type: "+type+" Runs: "+runs ;
	}
	
	
	
	
	
}
