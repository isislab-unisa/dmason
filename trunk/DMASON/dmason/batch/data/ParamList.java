package dmason.batch.data;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("paramList")
public class ParamList extends Param 
{

	@XStreamImplicit(itemFieldName="item")
	private List<String> values;
	
	public ParamList(String name, String type, int runs, List<String> values) 
	{
		super(name, type, runs, "list");
		// TODO Auto-generated constructor stub
		this.values = values;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return super.toString()+ " List [values=" + values + "]";
	}
	
	
	
	

}
