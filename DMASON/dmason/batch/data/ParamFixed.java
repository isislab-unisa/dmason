package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("paramFixed")
public class ParamFixed extends Param {

	private String value;
	

	public ParamFixed(String name, String type, int runs, String value) {
		super(name, type, runs, "fixed");
		// TODO Auto-generated constructor stub
		
		this.value = value;
	}
	

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+" value: "+value;
	}

	
	
}
