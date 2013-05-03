package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("paramRange")
public class ParamRange extends Param 
{
	private String start;
	private String end;
	private String increment;
	
	public ParamRange(String name, String type, int runs, String start,String end, String increment) 
	{
		super(name, type, runs, "range");
		// TODO Auto-generated constructor stub
		
		this.start = start;
		this.end = end;
		this.increment = increment;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getIncrement() {
		return increment;
	}

	public void setIncrement(String increment) {
		this.increment = increment;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString()+" start: "+start+" end: "+end+" increment: "+increment;
	}
	
	
	
	
	
	

}
