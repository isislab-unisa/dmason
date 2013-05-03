package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ParamDistribution extends Param 
{

	@XStreamAsAttribute
	private String distributionName;
	@XStreamAsAttribute
	private int numberOfValues;
	
	public ParamDistribution(String name, String type, int runs, String distName,int numOfVal) 
	{
		super(name, type, runs, "distribution");
		// TODO Auto-generated constructor stub
		this.distributionName = distName;
		this.numberOfValues = numOfVal;
	}

	public String getDistributionName() {
		return distributionName;
	}

	public void setDistributionName(String distributionName) {
		this.distributionName = distributionName;
	}
	
	public int getNumberOfValues() {
		return numberOfValues;
	}

	public void setNumberOfValues(int numberOfValues) {
		this.numberOfValues = numberOfValues;
	}

	@Override
	public String toString() {
		return super.toString() + " #Values: "+numberOfValues;
	}
	
	

}
