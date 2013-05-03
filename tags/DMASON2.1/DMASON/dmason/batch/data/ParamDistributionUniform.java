package dmason.batch.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ParamDistributionUniform")
public class ParamDistributionUniform extends ParamDistribution 
{

	private String a,b;
	
	public ParamDistributionUniform(String name, String type, int runs, String a, String b,int numOfVal) {
		super(name, type, runs,"uniform",numOfVal);
		// TODO Auto-generated constructor stub
		this.a = a;
		this.b = b;
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}

	@Override
	public String toString() {
		return super.toString()+ " Uniform [a=" + a + ", b=" + b + "]";
	}
	
	

}
