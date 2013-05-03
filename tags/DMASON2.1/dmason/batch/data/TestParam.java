package dmason.batch.data;

import java.util.List;

public class TestParam
{
	private GeneralParam genParams;
	private List<EntryParam<String, Object>> simParams;
	public TestParam(GeneralParam genParams,
			List<EntryParam<String, Object>> simParams) {
		super();
		this.genParams = genParams;
		this.simParams = simParams;
	}
	public TestParam() {
		// TODO Auto-generated constructor stub
	}
	public GeneralParam getGenParams() {
		return genParams;
	}
	public void setGenParams(GeneralParam genParams) {
		this.genParams = genParams;
	}
	public List<EntryParam<String, Object>> getSimParams() {
		return simParams;
	}
	public void setSimParams(List<EntryParam<String, Object>> simParams) {
		this.simParams = simParams;
	}
	
	

}
