package dmason.util.SystemManagement;


public class EntryVal <V,B>{

	private int num;
	private boolean flag;
	
	public EntryVal(){}
	
	public EntryVal(int num, boolean flag) {
		super();
		this.num = num;
		this.flag = flag;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public boolean isFlagTrue() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "EntryVal [num=" + num + ", flag=" + flag + "]";
	}	
	
	
}