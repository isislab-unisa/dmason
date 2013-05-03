package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParamDistributionNormal")
public class ParamDistributionNormal extends ParamDistribution 
{

	private String mean;
	private String stdDev;
	
	public ParamDistributionNormal(String name, String type, int runs,
			String mean, String stdDev, int numOfVal) {
		super(name, type, runs, "normal", numOfVal);
		// TODO Auto-generated constructor stub
		this.mean = mean;
		this.stdDev = stdDev;
	}

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getStdDev() {
		return stdDev;
	}

	public void setStdDev(String stdDev) {
		this.stdDev = stdDev;
	}

	@Override
	public String toString() {
		return super.toString()+ " Normal [mean=" + mean + ", stdDev=" + stdDev
				+ "]";
	}

	
}
