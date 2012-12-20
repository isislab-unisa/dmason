package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParamDistributionExponential")
public class ParamDistributionExponential extends ParamDistribution 
{

	private String lambda;
	
	public ParamDistributionExponential(String name, String type, int runs,
			String lambda, int numOfVal) {
		super(name, type, runs, "exponential", numOfVal);
		// TODO Auto-generated constructor stub
		this.lambda = lambda;
	}

	public String getLambda() {
		return lambda;
	}

	public void setLambda(String lambda) {
		this.lambda = lambda;
	}

	@Override
	public String toString() {
		return super.toString()+ " Exponential [lambda=" + lambda + "]";
	}

	
}
